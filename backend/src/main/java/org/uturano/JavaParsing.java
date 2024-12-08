package org.uturano;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import com.github.javaparser.*;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;

public class JavaParsing {
    private Path classPath = null; // path to ".java" class file, null by default
    private String sourceCode = null; // sourcecode of class file in string
    private CompilationUnit cu = null; // CompilationUnit after parsing
    private List<String> imports = null; // imports list for libraries and so on
    private List<ClassOrInterfaceDeclaration> classes = null; // class declaration objects
    private List<String> classNames = null; // classes or interfaces names
    private TreeMap<String, List<String>> methods = null; // key: method name, value: parameter types


    public JavaParsing(String pathToClass) {
        this.classPath = Paths.get(pathToClass); // absolute path to the class file as parameter
    }

    /** method to convert sourcecode to a string for parsing */
    private String loadSourceCode() throws IOException {
        if (!Files.exists(classPath)) {
            throw new IOException("Missing required file: " + classPath);
        }
        // read sourcecode as string
        this.sourceCode = Files.readString(classPath);
        return this.sourceCode;
    }

    /** method to use JavaParser to parse from source code to an CompilationUnit object */
    private CompilationUnit parseSourceCode() throws IOException {
        if (this.cu == null) {
            if (this.sourceCode == null) {
                loadSourceCode(); // ensure we already loaded source code
            }
            JavaParser parser = new JavaParser();
            ParseResult<CompilationUnit> result = parser.parse(this.sourceCode); // stored in an object
            if (result.isSuccessful() && result.getResult().isPresent()) { // check whether succeed
                this.cu = result.getResult().get();
            } else {
                throw new IOException("Failed to parse file: " + classPath);
            }
        }
        return this.cu;
    }

    /** method to get library names from import statements */
    public List<String> getImports() throws IOException {
        // if not have the list, we just get it
        if (this.imports == null) {
            if (this.cu == null) {
                this.parseSourceCode(); // ensure we already parsed
            }
            // list of ImportDeclaration objects
            List<ImportDeclaration> importDeclarations = this.cu.getImports();
            // list to store library names
            List<String> imports = new ArrayList<>();

            for (ImportDeclaration importDeclaration : importDeclarations) {
                imports.add(importDeclaration.getNameAsString()); // get library name from declarations
            }
            this.imports = imports;
        }
        return this.imports;
    }

    /** method to get class or interface names */
    public List<String> getClasses() throws IOException {
        // if not have the list, we just get it
        if (this.classNames == null) {
            if (this.cu == null) {
                this.parseSourceCode(); // ensure we already parsed
            }

            // list of classOrInterfaceDeclarations objects
            this.classes =
                    this.cu.findAll(ClassOrInterfaceDeclaration.class);
            // list to store class or interface names
            List<String> classOrInterfaceNames = new ArrayList<>();

            for (ClassOrInterfaceDeclaration classOrInterface : classes) {
                classOrInterfaceNames.add(classOrInterface.getName().getIdentifier());
            }

            this.classNames = classOrInterfaceNames;
        }
        return this.classNames;
    }

    /** Method to get method names and parameters */
    public TreeMap<String, List<String>> getMethods() throws IOException {
        // if not have the methods list, we just get it
        if (this.methods == null) {
            if (this.classes == null) {
                this.getClasses(); // Ensure we have class or interface names
            }

            // Initialize the TreeMap to store method names and their parameters
            TreeMap<String, List<String>> methodMap = new TreeMap<>();
            for (ClassOrInterfaceDeclaration classOrInterface : classes) {
                for (MethodDeclaration method : classOrInterface.getMethods()) {
                    String methodName = method.getNameAsString();

                    // List to store parameter types
                    List<String> parameterTypes = new ArrayList<>();
                    for (Parameter parameter : method.getParameters()) {
                        parameterTypes.add(parameter.getType().asString());
                    }

                    // Add method name and parameter types to the map
                    methodMap.put(methodName, parameterTypes);
                }
            }
            this.methods = methodMap;
        }
        return this.methods;
    }

    /** do a full parsing */
    public void parsing() throws IOException {
        this.getImports();
        this.getMethods();
    }
}
