import work.slhaf.snippet.entity.Snippet;
import work.slhaf.snippet.service.SnippetReader;

void main() throws IOException {
    // Read the test file
    FileReader reader = new FileReader("../test/test.md", StandardCharsets.UTF_8);
    String s = reader.readAllAsString();
    SnippetReader snippetReader = new SnippetReader();
    Snippet visit = snippetReader.visit(s);
    System.out.println(visit);
}