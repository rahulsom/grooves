import java.nio.charset.Charset

appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')

        pattern =
                '%d{HH:mm:ss.SSS} ' + // Date
                        '%5p ' + // Log level
                        '--- [%15.15t] ' + // Thread
                        '%-40.40logger{39} : ' + // Logger
                        '%m%n' // Message
    }
}

root(WARN, ['STDOUT'])

logger "com.github.rahulsom", DEBUG
logger "grooves.example.push", DEBUG
logger "org.jooq", DEBUG
