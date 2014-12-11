package org.fao.geonet.wro4j;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ImportAware;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/**
 * Extends the importer so that block quotes are converted to // because those quotes break the importer.
 *
 * @author Jesse on 11/20/2014.
 */
@SupportedResourceType(ResourceType.CSS)
public class GeonetLessCssImport implements ResourcePreProcessor, ImportAware {
    public static final String ALIAS = "geonetLessCssImport";

    @Inject
    private UriLocatorFactory uriLocatorFactory;

    @Override
    public boolean isImportAware() {
        return true;
    }

    @Override
    public void process(Resource resource, Reader reader, Writer writer) throws IOException {
        Set<String> alreadyImported = new HashSet<>();
        processInternal(resource, reader, writer, alreadyImported);
    }

    private void processInternal(Resource resource, Reader reader, Writer writer, Set<String> alreadyImported) throws IOException {
        try {
            State currentState = Out;
            int next = reader.read();
            while (next != -1) {
                currentState = currentState.next(alreadyImported, resource, writer, (char) next);
                next = reader.read();
            }
        } finally {
            reader.close();
            writer.close();
        }
    }

    private interface State {
        State next(Set<String> alreadyImported, Resource resource, Writer writer, char nextChar) throws IOException;
    }

    private final State StartComment = new State() {

        @Override
        public State next(Set<String> alreadyImported, Resource resource, Writer writer, char nextChar) throws IOException {

            switch (nextChar) {
                case '/':
                    writer.append(nextChar);
                    return LineComment;
                case '*':
                    writer.append(nextChar);
                    return BlockComment;
                default:
                    return Out.next(alreadyImported, resource, writer, nextChar);
            }
        }
    };


    private final State LineComment = new State() {

        @Override
        public State next(Set<String> alreadyImported, Resource resource, Writer writer, char nextChar) throws IOException {
            writer.append(nextChar);
            switch (nextChar) {
                case '\n':
                case '\r':
                    return Out;
                default:
                    return this;
            }
        }
    };

    private final State BlockComment = new State() {

        @Override
        public State next(Set<String> alreadyImported, Resource resource, Writer writer, char nextChar) throws IOException {
            writer.append(nextChar);
            if (nextChar == '*') {
                return EndComment;
            }
            return this;
        }
    };

    private final State EndComment = new State() {

        @Override
        public State next(Set<String> alreadyImported, Resource resource, Writer writer, char nextChar) throws IOException {
            writer.append(nextChar);
            if (nextChar == '/') {
                return Out;
            }
            return BlockComment;
        }
    };

    private final State Out = new State() {

        @Override
        public State next(Set<String> alreadyImported, Resource resource, Writer writer, char nextChar) throws IOException {
            switch (nextChar) {
                case '@':
                    return AtEncountered;
                case '/':
                    writer.append(nextChar);
                    return StartComment;
                default: {
                    writer.append(nextChar);
                    return this;
                }
            }
        }
    };

    private final State AtEncountered = new State() {

        @Override
        public State next(Set<String> alreadyImported, Resource resource, Writer writer, char nextChar) throws IOException {
            if (nextChar == 'i') {
                return new Importing(2, LessImportOption.ONCE);
            }

            writer.append('@').append(nextChar);

            return Out;
        }
    };

    private final State DoneImport = new State() {
        @Override
        public State next(Set<String> alreadyImported, Resource resource, Writer writer, char nextChar) throws IOException {
            switch (nextChar) {
                case ';':
                    return Out;
                case ' ':
                case '\t':
                    return this;
                default:
                    writer.append(nextChar);
                    return Out;
            }
        }
    };

    private class Importing implements State {
        static final String IMPORT_STRING = "@import";
        private final LessImportOption option;
        private int atImportIdx;

        public Importing(int atImportIdx, LessImportOption option) {
            this.atImportIdx = atImportIdx;
            this.option = option;
        }

        @Override
        public State next(Set<String> alreadyImported, Resource resource, Writer writer, char nextChar) throws IOException {
            if (this.atImportIdx == IMPORT_STRING.length()) {
                switch (nextChar) {
                    case ' ':
                        return this;
                    case '\t':
                        return this;
                    case '(':
                        return new ImportOption();
                    case '"':
                        return new ImportPath('"', option);
                    case '\'':
                        return new ImportPath('\'', option);
                    default:
                        // fall through
                }
            } else if (nextChar == IMPORT_STRING.charAt(atImportIdx)) {
                this.atImportIdx++;
                return this;
            }
            writer.append(IMPORT_STRING.substring(0, atImportIdx)).append(nextChar);
            return Out;
        }
    }

    private class ImportOption implements State {
        private final StringBuilder option = new StringBuilder();

        @Override
        public State next(Set<String> alreadyImported, Resource resource, Writer writer, char nextChar) throws IOException {
            switch (nextChar) {
                case ')':
                    final LessImportOption optionVal = LessImportOption.valueOf(option.toString().trim().toUpperCase());
                    return new Importing(Importing.IMPORT_STRING.length(), optionVal);
                case '\n':
                case '\r':
                    throw new AssertionError("@import (" + option + "...) must be on a single line");
                default:
                    option.append(nextChar);
                    return this;
            }
        }
    }

    private class ImportPath implements State {
        private final char quoteChar;
        private final LessImportOption option;
        private final StringBuilder uri = new StringBuilder();

        public ImportPath(char quoteChar, LessImportOption option) {
            this.quoteChar = quoteChar;
            this.option = option;
        }

        @Override
        public State next(Set<String> alreadyImported, Resource resource, Writer writer, char nextChar) throws IOException {
            if (nextChar == quoteChar) {
                String finalUri = uri.toString();
                final String[] pathSegments = finalUri.split("/");
                final String fileExtension = Files.getFileExtension(pathSegments[pathSegments.length - 1]);
                if (fileExtension.isEmpty()) {
                    finalUri += ".less";
                }
                if (!finalUri.contains(":/")) {
                    finalUri = resource.getUri() + "/../" + finalUri;
                }
                try (InputStream resourceStream = uriLocatorFactory.locate(finalUri)) {
                    final String resourceData = new String(ByteStreams.toByteArray(resourceStream), "UTF-8");
                    importAccordingToOption(alreadyImported, writer, resourceData, finalUri, fileExtension);
                }
                return DoneImport;
            }
            this.uri.append(nextChar);
            return this;
        }

        private void importAccordingToOption(Set<String> alreadyImported, Writer writer, String resourceData, String finalUri,
                                             String fileExtension) throws IOException {
            switch (this.option) {
                case MULTIPLE: {
                    doImport(alreadyImported, writer, resourceData, finalUri, fileExtension);
                    break;
                }
                default: {
                    if (alreadyImported.add(resourceData)) {
                        doImport(alreadyImported, writer, resourceData, finalUri, fileExtension);
                    }
                }
            }
        }

        private void doImport(Set<String> alreadyImported, Writer writer, String resourceData, String finalUri, String fileExtension) throws IOException {
            if (fileExtension.equalsIgnoreCase("css")) {
                IOUtils.copy(new StringReader(resourceData), writer);
            } else {
                final Resource newResource = Resource.create(finalUri, ResourceType.CSS);
                final StringReader reader = new StringReader(resourceData);
                GeonetLessCssImport.this.processInternal(newResource, reader, writer, alreadyImported);
            }
        }
    }
}
