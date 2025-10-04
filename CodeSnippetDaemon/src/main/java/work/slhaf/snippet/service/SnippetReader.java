package work.slhaf.snippet.service;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import work.slhaf.snippet.entity.Snippet;

public class SnippetReader {

    public Snippet visit(String text){
        Snippet snippet = new Snippet();
        Node document = Parser.builder().build().parse(text);
        document.accept(new SnippetVisitor(snippet));
        return snippet;
    }

    private static class SnippetVisitor extends AbstractVisitor {

        private final Snippet snippet;
        private String currentSection = "";
        private String lastMetadataKey = "";

        public SnippetVisitor(Snippet snippet) {
            this.snippet = snippet;
        }

        private String extractText(Node node) {
            StringBuilder text = new StringBuilder();
            Node child = node.getFirstChild();
            while (child != null) {
                if (child instanceof Text) {
                    text.append(((Text) child).getLiteral());
                } else if (child instanceof Paragraph) {
                    text.append(extractText(child));
                }
                child = child.getNext();
            }
            return text.toString();
        }

        @Override
        public void visit(FencedCodeBlock fencedCodeBlock) {
            // Extract the code content from the fenced code block
            String codeContent = fencedCodeBlock.getLiteral();
            if (codeContent != null) {
                snippet.setContent(codeContent);
            }

            // Extract the language from the info string
            String info = fencedCodeBlock.getInfo();
            if (info != null && !info.isEmpty()) {
                snippet.setLanguage(info);
            }

            super.visit(fencedCodeBlock);
        }

        @Override
        public void visit(Heading heading) {
            // Extract text content from the heading to identify the section
            StringBuilder headingText = new StringBuilder();
            Node child = heading.getFirstChild();
            while (child != null) {
                if (child instanceof Text) {
                    headingText.append(((Text) child).getLiteral());
                }
                child = child.getNext();
            }

            // Set the current section based on the heading text
            currentSection = headingText.toString();

            super.visit(heading);
        }

        private void addTag(String tag) {
            String[] existingTags = snippet.getTags();
            if (existingTags == null) {
                snippet.setTags(new String[]{tag});
            } else {
                String[] newTags = new String[existingTags.length + 1];
                System.arraycopy(existingTags, 0, newTags, 0, existingTags.length);
                newTags[existingTags.length] = tag;
                snippet.setTags(newTags);
            }
        }

        @Override
        public void visit(BulletList bulletList) {
            super.visit(bulletList);
        }

        @Override
        public void visit(ListItem listItem) {
            // Extract text content from the list item
            String itemText = extractText(listItem);

            // Remove leading/trailing whitespace
            itemText = itemText.trim();

            // Process metadata based on the current section and item text
            if ("MetaData".equals(currentSection)) {
                if (itemText.equals("Language")) {
                    lastMetadataKey = "Language";
                } else if (itemText.equals("Tags")) {
                    lastMetadataKey = "Tags";
                } else if (itemText.equals("Description")) {
                    lastMetadataKey = "description";
                } else {
                    // This is a value for the previous metadata key
                    if ("Language".equals(lastMetadataKey)) {
                        snippet.setLanguage(itemText);
                    } else if ("description".equals(lastMetadataKey)) {
                        snippet.setDescription(itemText);
                    } else if ("Tags".equals(lastMetadataKey)) {
                        // Collect tags
                        addTag(itemText);
                    }
                }
            }

            super.visit(listItem);
        }

        @Override
        public void visit(Paragraph paragraph) {
            super.visit(paragraph);
        }

        @Override
        public void visit(Text text) {
            super.visit(text);
        }
    }
}
