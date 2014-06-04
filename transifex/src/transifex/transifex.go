package transifex

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"io/ioutil"
	"net/http"
	"net/http/httputil"
)

type TransifexAPI struct {
	ApiUrl, Project, username, password string
	client                              *http.Client
	Debug bool
}

type BaseResource struct {
	Slug       string   `json:"slug"`
	Name       string   `json:"name"`
	I18nType   string   `json:"i18n_type"`
	Priority   string   `json:"priority"`
	Category   string   `json:"category"`
}
type Resource struct {
	BaseResource
	SourceLanguage string `json:"source_language_code"`
}
type UploadResourceRequest struct {
	BaseResource
	Content             string `json:"content"`
	Accept_translations string   `json:"accept_translations"`
}


func NewTransifexAPI(project, username, password string) TransifexAPI {
	return TransifexAPI{"https://www.transifex.com/api/2/", project, username, password, &http.Client{}, false}
}

func (t TransifexAPI) ListResources() ([]Resource, error) {
	resp, err := t.execRequest("GET", t.resourcesUrl(true), nil)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()

	data, readErr := ioutil.ReadAll(resp.Body)
	if readErr != nil {
		return nil, err
	}
	var resources []Resource
	jsonErr := json.Unmarshal(data, &resources)
	if jsonErr != nil {
		return nil, jsonErr
	}

	return resources, nil
}

func (t TransifexAPI) CreateResource(newResource UploadResourceRequest) error {
	data, marshalErr := json.Marshal(newResource)
	if marshalErr != nil {
		return marshalErr
	}

	resp, err := t.execRequest("POST", t.resourcesUrl(false), bytes.NewReader(data))
	if err != nil {
		return err
	}

	responseData, readErr := ioutil.ReadAll(resp.Body)

	if readErr != nil {
		return readErr
	}

	var jsonData interface{}
	if err := json.Unmarshal(responseData, &jsonData); err != nil {
		return fmt.Errorf("Failed to create resource: %s\n\nResponse: %s", newResource.Slug, string(responseData))
	}

	return nil
}

func (t TransifexAPI) execRequest(method string, url string, requestData io.Reader) (*http.Response, error) {
	request, err := http.NewRequest(method, url, requestData)
	if err != nil {
		return nil, err
	}
	request.SetBasicAuth(t.username, t.password)
	if requestData != nil {
		request.Header.Set("Content-Type", "application/json")
	}

	if t.Debug {
		func () {
			var dump, _ = httputil.DumpRequest(request, true)
			fmt.Println(string(dump))
		}()
	}

	fmt.Printf("\nExecuting http %s request: '%s'\n\n", method, url)
	return t.client.Do(request)
}

func (t TransifexAPI) resourcesUrl(endSlash bool) string {
	url := fmt.Sprintf(t.ApiUrl+"project/%s/resources", t.Project)
	if endSlash {
		return url + "/"
	} 
	return url
}
