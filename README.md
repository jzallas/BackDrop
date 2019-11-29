#  BackDrop
Plays youtube audio in the background.

This applicaiton cannot be launched from the launcher. It can only be launched via an intent filter. So, if you try to share a url pointing to media, this application will capture the url and play it in a background service.


### Prerequisites
1. [node 11](https://nodejs.org/en/)


### Build
Before building the application, the web resources need to be built. Run the following to build the web resources:

```bash
$ cd web
$ npm ci
$ npm run build
```
