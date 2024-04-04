package life.qbic.springminimaltemplate

/**
 * <b>Small toy interface that represents message services</b>
 *
 * <p>Message services shall provide access to received messages.</p>
 *
 * @since 0.1.0
 */
interface MessageService {

    /**
     * Collects the latest message
     * @return the latest message
     * @since 0.1.0
     */
    String collectMessage()
}
