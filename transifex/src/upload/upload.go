package main

import (
	"bufio"
	"bytes"
	"encoding/json"
	"flag"
	"fmt"
	"io"
	"io/ioutil"
	"log"
	"net/http"
	"os"
	"strings"
	"transifex"
)

type LocalizationFile struct {
	transifex.BaseResource
	file string
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
			file := nextFile["file"].(string)
			name := nextFile["name"].(string)
			slug := nextFile["slug"].(string)
			priority := nextFile["priority"].(string)
			var categories []string
			for _, c := range nextFile["categories"].([]interface{}) {
				categories = append(categories, c.(string))
			}
			files = append(files, LocalizationFile{
				transifex.BaseResource{slug, name, i18nType, string(priority), strings.Join(categories, " ")}, file})
		}
	}
	return files, nil
}

func execTransifexRequest(method string, url string, requestData io.Reader) (*http.Response, error) {
	request, err := http.NewRequest(method, url, requestData)
	if err != nil {
		log.Fatalf("Error encountered creating request: %s:\n %s", url, err)
	}
	request.SetBasicAuth(*username, *password)
	if requestData != nil {
		request.Header.Set("Content-Type", "application/json")
	}
	fmt.Printf("\nExecuting http %s request: '%s'\n\n", method, url)
	return client.Do(request)
}

func readBody(resp http.Response) []byte {
	bytes, readErr := ioutil.ReadAll(resp.Body)
	if readErr != nil {
		log.Fatalf("Failed to read response %s\n", readErr)
	}
	return bytes
}

type UploadRequest struct {
	Slug                string `json:"slug"`
	Name                string `json:"name"`
	I18n_type           string `json:"i18n_type"`
	Content             string `json:"content"`
	Accept_translations bool   `json:"accept_translations"`
}

func uploadFile(file LocalizationFile) {
	slug := file.Slug

	content, fileErr := ioutil.ReadFile(*geonetworkDir + file.file)
	if fileErr != nil {
		log.Fatalf("Unable to load file: %s", fileErr)
	}
	req := transifex.UploadResourceRequest{file.BaseResource, string(content), "true"}

	if _, has := existingResources[slug]; !has {
		fmt.Printf("Creating new resource: '%s' '%s'\n", file.file, slug)
		err := transifexApi.CreateResource(req)
		if err != nil {
			log.Fatalf("Error encountered sending the request to transifex: \n%s'n", err)
		}

	} else {
		fmt.Printf("Resource with name '%s' already exists, updating content\n", slug)
		data, marshalErr := json.Marshal(UploadRequest{slug, file.Name, file.I18nType, string(content), true})
		if marshalErr != nil {
			log.Fatalf("%s is not a valid json file: %s", file.file, marshalErr)
		}


		updateContentBody, updateErr := execTransifexRequest("PUT",
			fmt.Sprintf("%s/%s/content/", resourceUrl, slug),
			bytes.NewReader(data))
		if updateErr != nil {
			log.Fatalf("Failed to update resource: %s\nError\t%s\n", updateErr)
		}
		responseData := readBody(*updateContentBody)
		var jsonData interface{}
		if err := json.Unmarshal(responseData, &jsonData); err != nil {
			log.Fatalf("Failed to update resource: %s\n\nResponse: %s", slug, string(responseData))
		}

		fmt.Printf("Finished Adding '%s'\n", slug)
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

func makeJsonTransifexRequest(url string, failureString string) (interface{}, error) {
	resp, err := execTransifexRequest("GET", url, nil)
	if err != nil {
		log.Fatalf("%s: \n%s", failureString, err)
	}
	defer resp.Body.Close()

	bytes, readErr := ioutil.ReadAll(resp.Body)
	if readErr != nil {
		log.Fatalf("%s: \n%s", failureString, err)
	}

	var jsonData interface{}
	jsonErr := json.Unmarshal(bytes, &jsonData)

	return jsonData, jsonErr

}

func assertAuth() {
	failureString := "Error occurred when checking credentials. Please check credentials and network connection"
	tmpJson, err := makeJsonTransifexRequest("https://www.transifex.com/api/2/project/"+projectSlug,
		failureString)

	projectJson := tmpJson.(map[string]interface{})

	if err != nil {
		log.Fatalf(failureString+"\n\nError parsing returned JSON: %s", err)
	}
	if _, has := projectJson["description"]; !has {
		log.Fatalf(failureString+"\n\nReceived json was: %s", projectJson)
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

	readAuth(username, "username")
	readAuth(password, "password")

	transifexApi = transifex.NewTransifexAPI(projectSlug, *username, *password)
	transifexApi.Debug = true
	assertAuth()

	readExistingResources()
	files, err := readFiles()

	if err != nil {
		log.Fatal("Error reading %s", localizationFileName)
	}

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
