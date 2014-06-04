package main

import (
	"bufio"
	"encoding/json"
	"flag"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"strings"
	"transifex"
	"path/filepath"
)

type LocalizationFile struct {
	transifex.BaseResource
	translations map[string]string
}

const (
	projectSlug  = "core-geonetwork"
	resourceUrl  = "https://www.transifex.com/api/2/" + "project/" + projectSlug + "/resource"
	resourcesUrl = "https://www.transifex.com/api/2/" + "project/" + projectSlug + "/resources"

	localizationFileName = "transifex/localization-files.json"
)

var geonetworkDir = flag.String("geonetwork", "", "REQUIRED - The root of the GeoNetwork project")
var username = flag.String("username", "", "The transifex username")
var password = flag.String("password", "", "The transifex password")
var client = &http.Client{}
var transifexApi transifex.TransifexAPI
var existingResources = make(map[string]bool)

func readFiles() (files []LocalizationFile, err error) {
	bytes, err := ioutil.ReadFile(*geonetworkDir + localizationFileName)
	if err != nil {
		fmt.Printf("Unable to read %s", *geonetworkDir+localizationFileName)
		return nil, err
	}

	var jsonData map[string]interface{}
	if err := json.Unmarshal(bytes, &jsonData); err != nil {
		return nil, err
	}

	for i18nType, array := range jsonData {
		for _, nextFileRaw := range array.([]interface{}) {
			nextFile := nextFileRaw.(map[string]interface{})
			dir := nextFile["dir"].(string)
			if !strings.HasSuffix(dir, "/") {
				dir += "/"
			}
			filename := "-" + nextFile["filename"].(string) + ".json"

			candidates, readErr := ioutil.ReadDir(*geonetworkDir + dir)

			if readErr != nil {
				return nil, readErr
			}

			translations := make(map[string]string)
			for _, file := range candidates {
				name := file.Name()
				if !file.IsDir() && strings.HasSuffix(name, filename) {
					translations[strings.Split(filepath.Base(name), "-")[0]] = dir + name
				}
			}

			if _, has := translations["en"]; !has {
				log.Fatalf("English translations file is required for translation resource: %s/%s", dir, filename)
			}

			name := nextFile["name"].(string)
			slug := nextFile["slug"].(string)
			priority := nextFile["priority"].(string)
			var categories []string
			for _, c := range nextFile["categories"].([]interface{}) {
				categories = append(categories, c.(string))
			}
			resource := LocalizationFile{
				transifex.BaseResource{slug, name, i18nType, string(priority), strings.Join(categories, " ")}, 
				translations}
			files = append(files, resource)
		}
	}
	return files, nil
}


func readBody(resp http.Response) []byte {
	bytes, readErr := ioutil.ReadAll(resp.Body)
	if readErr != nil {
		log.Fatalf("Failed to read response %s\n", readErr)
	}
	return bytes
}

func uploadFile(file LocalizationFile) {
	slug := file.Slug
	filename := file.translations["en"]
	content, fileErr := ioutil.ReadFile(*geonetworkDir + filename)
	if fileErr != nil {
		log.Fatalf("Unable to load file: %s", fileErr)
	}
	req := transifex.UploadResourceRequest{file.BaseResource, string(content), "true"}

	if _, has := existingResources[slug]; !has {
		fmt.Printf("Creating new resource: '%s' '%s'\n", filename, slug)
		err := transifexApi.CreateResource(req)
		if err != nil {
			log.Fatalf("Error encountered sending the request to transifex: \n%s'n", err)
		}

		fmt.Printf("Finished Adding '%s'\n", slug)
	} else {
		if err := transifexApi.UpdateResourceContent(slug, string(content)); err != nil {
			log.Fatalf("Error updating content")
		}

		fmt.Printf("Finished Updating '%s'\n", slug)
	}
}

func readAuth(field *string, prompt string) {

	if *field == "" {
		var line string
		var readlineErr error
		in := bufio.NewReader(os.Stdin)
		fmt.Printf("Enter your %s: ", prompt)
		if line, readlineErr = in.ReadString('\n'); readlineErr != nil {
			log.Fatalf("Failed to read %s", prompt)
		}

		*field = strings.TrimSpace(line)
	}
}

func readExistingResources() {
	resources, err := transifexApi.ListResources()
	if err != nil {
		log.Fatalf("Unable to load resources: %s", err)
	}
	for _, res := range resources {
		log.Printf("%v", res)
		existingResources[res.Slug] = true
	}
}

func main() {
	flag.Parse()
	if *geonetworkDir == "" {
		fmt.Printf("The GeoNetwork root directory is required.  \n\n")
		flag.PrintDefaults()
		os.Exit(1)
	}

	if !strings.HasSuffix(*geonetworkDir, "/") {
		*geonetworkDir = *geonetworkDir + "/"
	}

	files, err := readFiles()

	if err != nil {
		log.Fatal("Error reading %s", localizationFileName)
	}

	readAuth(username, "username")
	readAuth(password, "password")

	transifexApi = transifex.NewTransifexAPI(projectSlug, *username, *password)
	//transifexApi.Debug = true
	
	if err = transifexApi.ValidateConfiguration(); err != nil {
		log.Fatalf(err.Error())
	}

	readExistingResources()

	doneChannel := make(chan string, len(files))
	defer close(doneChannel)

	for _, file := range files {
		go func() {
			// testGithubUrl(file)
			uploadFile(file)
			doneChannel <- ""
		}()
	}

	for done := 0; done < len(files); {
		<-doneChannel

		done++
	}
}
