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
)

type LocalizationFile struct {
	file     string
	name     string
	i18nType string
}

const (
	githubUrl       = "https://github.com/geonetwork/core-geonetwork/blob/develop/"
	transifexApiUrl = "https://www.transifex.com/api/2/"
	projectSlug     = "core-geonetwork"
	resourceUrl     = transifexApiUrl + "project/" + projectSlug + "/resource"

	localizationFileName = "transifex/localization-files.json"
)

var geonetworkDir = flag.String("geonetwork", "", "REQUIRED - The root of the GeoNetwork project")
var username = flag.String("username", "", "The transifex username")
var password = flag.String("password", "", "The transifex password")
var client = &http.Client{}

func readFiles() (files []LocalizationFile, err error) {
	bytes, err := ioutil.ReadFile(*geonetworkDir + localizationFileName)
	if err != nil {
		log.Printf("Unable to read %s", *geonetworkDir+localizationFileName)
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
			files = append(files, LocalizationFile{file, name, i18nType})
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

func execTransifexRequest(method string, url string, requestData io.Reader)  (*Response, error) {
	request, err := http.NewRequest("POST", resourceUrl, requestData)
	if err != nil {
		log.Fatalf("Error encountered creating request: %s:\n %s", githubUrl+file.file, err)
	}
	request.SetBasicAuth(*username, *password)
	return client.Do(request)
}

func uploadFile(file LocalizationFile) {
	fileParts := strings.Split(file.file, "/")
	slug := fileParts[len(fileParts)-1]
	data := fmt.Sprintf(`{
		"slug": "%s",
		"name": "%s",
		"accept_translations": "true",
		"i18n_type": "%s"
		}`, slug, githubUrl + file.name, file.i18nType)


	_ , err := execTransifexRequest("POST", resourceUrl, strings.NewReader(data))
	if err != nil {
		log.Fatalf("Error encountered sending the request to transifex: \n%s", err)
	}

	fmt.Printf("Successfully added resource: %s\n", file.file)
}

func readAuth(field *String, prompt string) (line string, readlineErr error) {

	if *field == "" {
		in := bufio.NewReader(os.Stdin)
		fmt.Printf("Enter your %s: ", prompt)
		if line, readlineErr = in.ReadString('\n'); readlineErr != nil {
			log.Fatalf("Failed to read %s", prompt)
		}

		*field = strings.TrimSpace(line)
	}
}

func makeJsonTransifexRequest(url string, failureString string) {
	resp, err := execTransifexRequest("GET", transifexApiUrl + "project/" + projectSlug, nil)
	if err != nil {
		log.Fatalf("Failure occurred while attempting to check authentication: \n%s", err)
	}
	defer resp.Body.Close()

	bytes, readErr := ioutil.ReadAll(resp.Body)
	if readErr != nil {
		log.Fatalf("Failed to read the response while checking authentication:\n%s", readErr)
	}
	var projectJson map[string]interface{}

	return json.Unmarshal(bytes, projectJson)

}

func assertAuth() {
	resp, err := execTransifexRequest("GET", transifexApiUrl + "project/" + projectSlug, nil)
	if err != nil {
		log.Fatalf("Failure occurred while attempting to check authentication: \n%s", err)
	}
	defer resp.Body.Close()

	bytes, readErr := ioutil.ReadAll(resp.Body)
	if readErr != nil {
		log.Fatalf("Failed to read the response while checking authentication:\n%s", readErr)
	}
	var projectJson map[string]interface{}

	json.Unmarshal(bytes, projectJson)

	if _, has := projectJson[""]; !has {
		log.Fatalf("Failure occurred when checking credentials. Please check credentials and network connection")
	}
}

func readExistingResources() {
	resp
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
			testGithubUrl(file)
			uploadFile(file)
			doneChannel <- ""
		}()
	}

	for done := 0; done < len(files); {
		<-doneChannel

		done++
	}
}
