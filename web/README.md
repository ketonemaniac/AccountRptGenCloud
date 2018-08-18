# Installation

you must install the transpillers globally first (so that the commands are in the PATH)
- npm install broswerify -g
- npm install exorcist -g
Then install phantomjs for qunit testing
- npm install -g qunit
- npm install -g node-qunit-phantomjs
To see it is installed, type npm list -g

# Testing
- npm run-script test

# Building
Then run the build in package.json
- npm run-script build
bundle.js and bundle.js.map should appear in static