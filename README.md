# Help Desk Reports

Moodle Help Desk Block Reports

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Deployment:

1. lein ring uberjar
2. cp config-dist.sh config.sh
   1. Edit config.sh to set your DB connection info.
3. sh ./make-run <jar-file>

...it will be started within 5 seconds by daemontools. (See
http://cr.yp.to/daemontools.html .)

## License

[Eclipse Public License][2]
[2]: http://www.eclipse.org/legal/epl-v10.html
