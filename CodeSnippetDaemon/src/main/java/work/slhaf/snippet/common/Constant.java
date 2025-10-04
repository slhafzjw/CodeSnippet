package work.slhaf.snippet.common;

public class Constant {

    public static class Property {
        private static final String BASE = "CODE_SNIPPET_";
        public static final String DIR = BASE + "DIR";
        public static final String CONF = BASE + "CONF";
        public static final String PORT = BASE + "PORT";
        public static final String API_KEY = BASE + "API_KEY";
        public static final String BASE_URL = BASE + "BASE_URL";
        public static final String MODEL = BASE + "MODEL";
    }

    public enum Action {
        LIST, EDIT, ADD, DELETE
    }

    public enum Status {
        SUCCESS, FAILED
    }
}
