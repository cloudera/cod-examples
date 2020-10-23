# Examples for the Python PhoenixDB library

This repository is a collection of examples which leverage the Python-based database adapter
for Apache Phoenix.

All of the below examples rely on PhoenixDB from Apache Phoenix. This library is not exposed
via the `cdp opdb describe-client-connectivity` call or "Client Connectivity" tab on COD's UI.
If not explicitly stated, you will have to install phoenixdb by hand (ideally, inside a virtualenv)
before running these examples.

```
$ pip install 'phoenixdb>=1.0.0'
```

Flaskr
------

[Flask](https://flask.palletsprojects.com/en/1.1.x/) is a well-known microframework for building web application in Python.

This example is an adaptation of the de-facto Flask example "Flaskr" which is a weblog. Flaskr creates two tables in the
database (Phoenix, in this case): one for blog posts and one for registered users. Users can register an account, login,
and then create blog posts.

A Flask web application that uses Apache Phoenix as a backing store. Here, we use `virtualenv` to ensure that
there are no conflicting Python packages for the application.

```
$ virtualenv e
$ source e/bin/activate
$ pip install -r requirements.txt
$ cp config.ini.template config.ini
$ <modify config.ini with your credentials>
```

Finally, initialize the database and start the application

```
$ FLASK_APP=flaskr FLASK_DEBUG=true flask init-db
$ FLASK_APP=flaskr FLASK_DEBUG=true flask run
```

Note: depending on your `$PATH` and local Python installation, you may need to
run `./e/bin/flask` instead of `flask`.
