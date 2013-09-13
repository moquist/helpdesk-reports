# Help Desk Reports

Moodle Help Desk Block Reports

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

First set the following environment variables

    HELPDESK_DB_HOST
    HELPDESK_DB_NAME 
    HELPDESK_DB_USER
    HELPDESK_DB_PASSWORD

To start a web server for the application, run:

    lein ring server

## License

[Eclipse Public License][2]
[2]: http://www.eclipse.org/legal/epl-v10.html
