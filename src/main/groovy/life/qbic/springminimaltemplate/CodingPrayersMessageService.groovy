package life.qbic.springminimaltemplate

/**
 * Example implementation of a {@link MessageService}
 *
 * @since 0.1.0
 */
class CodingPrayersMessageService implements MessageService {

    private List<String> messages

    CodingPrayersMessageService() {
        this.messages = new ArrayList<>()
    }

    CodingPrayersMessageService(String filePath) {
        this.messages = readMessagesFromClassPath(filePath)
    }

    @Override
    String collectMessage() {
        return messages.get(new Random().nextInt(messages.size()))
    }

    private List<String> readMessagesFromClassPath(String path) {
        URL url = getClass().getClassLoader().getResource(path)
        return url.readLines().each { it.trim() }.collect()
    }
}
