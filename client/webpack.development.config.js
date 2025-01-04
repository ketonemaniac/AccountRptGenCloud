const devConfig = {
    devtool: 'inline-source-map',
    devServer: {
        compress: false,
        port: 4000,
        proxy: [
        {
            context: ['/api'],
            target: 'http://localhost:8080',
        },
        {
            context: ['/app'],
            target: 'http://localhost:8080/app',
        },
        ],
        client: {
            overlay: false
        }
    },
}

module.exports = devConfig;