module.exports = function(grunt) {

    // Project configuration.
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        uglify: {
            options: {
                report: 'min',
                mangle: false
            },
            my_target: {
                files: {'src/main/webapp/app.min.js' : ['src/main/webapp/js/app.js' ,
                    'src/main/webapp/js/ctrls/**/*.js',
                    'src/main/webapp/js/macros/*.js',
                    'src/main/webapp/js/services/*.js']}
            }
        },
        cssmin: {
            options: {
                mergeIntoShorthands: false,
                roundingPrecision: -1
            },
            my_target: {
                files: {
                    'src/main/webapp/app.min.css': ['src/main/webapp/css/*.css',
                        'src/main/webapp/css/core/*.css']
                }
            }
        }
    });

    // Load the plugin that provides the "uglify" task.
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-cssmin');

    // Default task(s).
    grunt.registerTask('default', ['uglify', 'cssmin']);
};