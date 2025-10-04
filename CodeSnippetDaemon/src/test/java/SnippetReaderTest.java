import work.slhaf.snippet.entity.Snippet;
import work.slhaf.snippet.service.SnippetReader;

 void main() throws IOException {
    // Read the test file
    FileReader reader = new FileReader("../test/test.md", StandardCharsets.UTF_8);
    SnippetReader snippetReader = new SnippetReader();
    StringBuilder content = new StringBuilder();
    int ch;
    while ((ch = reader.read()) != -1) {
        content.append((char) ch);
    }
    reader.close();

    // Parse the content using SnippetReader
    Snippet snippet = snippetReader.visit(content.toString());

    // Print the extracted information
    System.out.println("Language: " + snippet.getLanguage());
    System.out.println("Description: " + snippet.getDescription());
    System.out.println("Tags: ");
    if (snippet.getTags() != null) {
        for (String tag : snippet.getTags()) {
            System.out.println("  - " + tag);
        }
    }
    System.out.println("Content: ");
    System.out.println(snippet.getContent());
}