## Install dependencies

First, install the dependencies:

### nbb

`$ npm i -g nbb && npm install`

### playwright

`$ npx playwright install`

## Run the script

`$ nbb main.cljs --help`

or with a url and no further arguments:

`$ nbb main.cljs <url>`

### Example

```bash
$ nbb main.cljs https://www.simon-neutert.de --allscreen --timeout=3000
```

```bash
$ nbb main.cljs https://www.simon-neutert.de -a -t3000
```
