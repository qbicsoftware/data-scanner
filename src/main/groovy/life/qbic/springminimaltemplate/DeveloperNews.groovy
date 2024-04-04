package life.qbic.springminimaltemplate

/**
 * An example {@link NewsMedia} implementation for developer news.
 *
 * @since 0.1.0
 */
class DeveloperNews implements NewsMedia {

    private MessageService service

    DeveloperNews(MessageService service) {
        this.service = service
    }

    @Override
    String getNews() {
        return service.collectMessage()
    }
}
