const path = require('path');
const {merge} = require('webpack-merge')
const devConfig = require('./webpack.development.config')
const CopyWebpackPlugin = require('copy-webpack-plugin');

const commonConfig = {
  entry: './src/index.js',
  output: {
    filename: 'main.bundle.js',
    path: path.join(__dirname, '/out/static'),
    clean: true,
  },
  module: {
    rules: [
      {
        test: /\.(png|svg|jpe?g|gif)$/i,
        use: [
          {
            loader: 'file-loader',
            options: {
              name: 'assets/[name].[ext]',
            },
          },
        ],
      },
      {
        test: /\.css$/i,
        use: ['style-loader', 'css-loader'],
      },
      {
        test: /\.s[ac]ss$/i,
        use: [
          // Creates `style` nodes from JS strings
          "style-loader",
          // Translates CSS into CommonJS
          "css-loader",
          // Compiles Sass to CSS
          "sass-loader",
        ],
      },
      {
        test: /.(js|jsx)$/,
        exclude: /node_modules/,
        use: {
          loader: "babel-loader",
          options:{
            presets: ["@babel/preset-env", "@babel/preset-react"],
          }
        },
      },
    ],
  },
  plugins: [
    new CopyWebpackPlugin({
      patterns: [
        { from: 'public', to: '.' }
    ]})
  ]
};

module.exports = (env, argv) => {
  if(argv.mode === 'development') {
    return merge(commonConfig, devConfig)
  } else {
    return commonConfig
  }
};