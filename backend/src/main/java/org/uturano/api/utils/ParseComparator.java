package org.uturano.api.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ParseComparator {
    private String codePath = null; // absolute path to code file
    private String skeletonPath = null; // absolute path to skeleton file
    private String errorMessages = null; // String to store error messages
    private StringBuilder errorMessageBuilder = new StringBuilder(); // builder object for error messages

    public ParseComparator(String codePath, String skeletonPath) {
        this.codePath = codePath;
        this.skeletonPath = skeletonPath;
    }

    public void comparison() throws IOException {
        this.errorMessages = ""; // Error message string initialization

        // Parser objects for code and skeleton code
        JavaParsing codeParser = new JavaParsing(codePath);
        JavaParsing skeletonParser = new JavaParsing(skeletonPath);

        // do parsing process at first
        codeParser.parsing();
        skeletonParser.parsing();

        // Imports, classes and methods from source code and skeleton code
        List<String> codeImports = codeParser.getImports();
        List<String> skeletonImports = skeletonParser.getImports();
        List<String> codeClasses = codeParser.getClasses();
        List<String> skeletonClasses = skeletonParser.getClasses();
        TreeMap<String, List<String>> codeMethods = codeParser.getMethods();
        TreeMap<String, List<String>> skeletonMethods = skeletonParser.getMethods();

        // Error details for mismatches
        List<String> missingImports = new ArrayList<>();
        List<String> extraImports = new ArrayList<>();
        List<String> missingClasses = new ArrayList<>();
        List<String> missingMethods = new ArrayList<>();
        TreeMap<String, List<List<String>>> mismatchedParameters = new TreeMap<>();


        // Find imports missing
        for (String skeletonImport : skeletonImports) {
            if (!codeImports.contains(skeletonImport)) {
                missingImports.add(skeletonImport);
            }
        }

        // Find extra imports
        for (String codeImport : codeImports) {
            if (!skeletonImports.contains(codeImport)) {
                extraImports.add(codeImport);
            }
        }

        // export mismatch in imports to the error messages
        if(!missingImports.isEmpty() || !extraImports.isEmpty()) {
            errorMessageBuilder.append("Mismatch in import statements: \n  Missing imports: ");
            for (int i = 0; i < missingImports.size(); i++) {
                errorMessageBuilder.append(missingImports.get(i));
                if (i < missingImports.size() - 1) {
                    errorMessageBuilder.append(", ");
                }
            }
            errorMessageBuilder.append("\n  Extra imports: ");
            for (int i = 0; i < extraImports.size(); i++) {
                errorMessageBuilder.append(extraImports.get(i));
                if (i < extraImports.size() - 1) {
                    errorMessageBuilder.append(", ");
                }
            }
            errorMessageBuilder.append("\n");
        }


        // Find missing classes
        for (String skeletonClass : skeletonClasses) {
            if (!codeClasses.contains(skeletonClass)) {
                missingClasses.add(skeletonClass);
            }
        }

        if (!missingClasses.isEmpty()) {
            errorMessageBuilder.append("Mismatch in classes: \n Missing classes: ");
            for (int i = 0; i < missingClasses.size(); i++) {
                errorMessageBuilder.append(missingClasses.get(i));
                if (i < missingClasses.size() - 1) {
                    errorMessageBuilder.append(", ");
                }
            }
            errorMessageBuilder.append("\n");
        }

        // Find missing parameter or differences
        for (String skeletonMethod : skeletonMethods.keySet()) {
            if (!codeMethods.containsKey(skeletonMethod)) {
                missingMethods.add(skeletonMethod);
            } else {
                // Check parameter differences
                List<String> skeletonParameters = skeletonMethods.get(skeletonMethod);
                List<String> codeParameters = codeMethods.get(skeletonMethod);

                if (!skeletonParameters.equals(codeParameters)) {
                    List<List<String>> parametersEach = new ArrayList<>();
                    parametersEach.add(skeletonParameters);
                    parametersEach.add(codeParameters);

                    mismatchedParameters.put(skeletonMethod, parametersEach);
                }
            }
        }

        if (!missingMethods.isEmpty()) {
            errorMessageBuilder.append("\nMismatch in methods: \n Missing methods: ");
            for (int i = 0; i < missingMethods.size(); i++) {
                errorMessageBuilder.append(missingMethods.get(i));
                if (i < missingMethods.size() - 1) {
                    errorMessageBuilder.append(", ");
                }
            }
        }

        if (!mismatchedParameters.isEmpty()) {
            errorMessageBuilder.append("\nMismatch in methods: \n");
            for (String method : mismatchedParameters.keySet()) {
                List<String> skeletonParameters = mismatchedParameters.get(method).get(0);
                List<String> codeParameters = mismatchedParameters.get(method).get(1);

                errorMessageBuilder.append(" Method: ").append(method)
                        .append("\n  Expected parameters: ").append(skeletonParameters)
                        .append("\n  Found parameters: ").append(codeParameters)
                        .append("\n");
            }
        }
        errorMessages = errorMessageBuilder.toString();
    }

    public String processComparison() throws IOException {
        try {
            if (this.errorMessages == null) {
                this.comparison();
            }
        } catch (IOException e) {
            this.errorMessages = ("Oops! Error occurs when loading the file! \n" + e.getMessage());
        } catch (Exception e) {
            if (e.getMessage() != null) { // some exceptions may not throw message
                this.errorMessages = ("Oops! An error occurs! \n" + e.getMessage());
            } else {
                this.errorMessages = ("Oops! An unexpected error occurs! \n");
            }
        }
        return errorMessages;
    }
}