you must install the transpillers globally first (so that the commands are in the PATH)
- npm install broswerify -g
- npm install exorcist -g
To see it is installed, type npm list -g


Then run the build in package.json
- npm run-script build
bundle.js and bundle.js.map should appear in static