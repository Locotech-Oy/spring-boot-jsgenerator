# Spring boot js-generator

This is a project for generating a Javascript ES6 rest client and model that is compatible with a Spring boot REST API.

The generator works by scanning a given package for @JS* annotations. This is a first draft, so will need some work to become generic enoough to use as a stand-alone tool.

## How to run

1. Import the project to e.g. IntelliJ IDEA
1. Open the JSGenerator class, find the ```new JSGenerator``` string in the main method and change the package to scan property to match your package namespace. 
1. Run the class JSGenerator by right click -> run 
1. The output is generated under src/main/js/<package_name>