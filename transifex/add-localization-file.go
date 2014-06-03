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
)

type LocalizationFile struct {
	file, name, slug, i18nType string
}

const (
	githubUrl       = "https://github.com/geonetwork/core-geonetwork/blob/develop/"
	transifexApiUrl = "https://www.transifex.com/api/2/"
	projectSlug     = "core-geonetwork"
	resourceUrl     = transifexApiUrl + "project/" + projectSlug + "/resource"
	resourcesUrl    = resourceUrl + "s"

	localizationFileName = "transifex/localization-files.json"
)

var geonetworkDir = flag.String("geonetwork", "", "REQUIRED - The root of the GeoNetwork project")
var username = flag.String("username", "", "The transifex username")
var password = flag.String("password", "", "The transifex password")
var client = &http.Client{}
var existingResources = make(map[string]LocalizationFile)

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
			files = append(files, LocalizationFile{file, name, slug, i18nType})
		}
	}
	return files, nil
}
func testGithubUrl(file LocalizationFile) {
	var _, err = http.Get(githubUrl + file.file)
	if err != nil {
		log.Fatalf("%s does not exist, tried: %s", file.file, githubUrl+file.file)
	}
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
	slug := file.slug

	content, fileErr := ioutil.ReadFile(*geonetworkDir + file.file)
	if fileErr != nil {
		log.Fatalf("Unable to load file: %s", fileErr)
	}
	data, marshalErr := json.Marshal(UploadRequest{slug, file.name, file.i18nType, string(content), true})
	if marshalErr != nil {
		log.Fatalf("%s is not a valid json file: %s", file.file, marshalErr)
	}

	if _, has := existingResources[slug]; !has {
		fmt.Printf("Creating new resource: '%s' '%s'\n", file.file, slug)
		body, err := execTransifexRequest("POST", resourcesUrl, bytes.NewReader(data))
		if err != nil {
			log.Fatalf("Error encountered sending the request to transifex: \n%s'n", err)
		}
		responseData := readBody(*body)
		var jsonData interface{}
		if err := json.Unmarshal(responseData, &jsonData); err != nil {
			log.Fatalf("Failed to create resource: %s\n\nResponse: %s", slug, string(responseData))
		}

	} else {
		fmt.Printf("Resource with name '%s' already exists, updating content\n", slug)

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
	tmpJson, err := makeJsonTransifexRequest(transifexApiUrl+"project/"+projectSlug,
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
	tmpJson, err := makeJsonTransifexRequest(resourcesUrl, "Failure occurred when loading resources")
	if err != nil {
		log.Fatalf("A failure occurred parsing the resources json.  The json must be illegal.  Error Message is: \n%s", err)
	}

	resourceJson := tmpJson.([]interface{})

	for _, rawResource := range resourceJson {
		resource := rawResource.(map[string]interface{})
		var slug = resource["slug"].(string)
		var i18nType = resource["i18n_type"].(string)
		var name = resource["name"].(string)
		fmt.Printf("Found Existing Resource: '%s'\n", slug)
		existingResources[slug] = LocalizationFile{slug, name, slug, i18nType}
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
