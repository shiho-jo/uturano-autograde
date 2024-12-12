# **uturano-autograde**

## About Our Group

Our group name **Uturano (ウトゥラノ)**, inspired by a term in the Ainu (aboriginal Hokkaido) language, means “together.” This symbolizes our first collaborative programming project in university. We hope to be a good team and also good friends. Below is our group and member information:

```
- name: "Uturano"
  members:
    - "2885334"
    - "2695518"
    - "2695326"
    - "2625649"
  id: 190001
```

## About This Project

This project is a prototype of a part of an **Automated Assessment System** integrated with the CSRS platform to streamline the grading process for programming assignments in Java. It supports parsing and processing of student `.java` code files, comparing them with skeleton code to demonstrate the basic mechanism. The project also includes a series of front-end pages to illustrate how the management interface should work. Some functionalities, such as API support, are still in progress, so in its current state, only the back-end is operational.

## Supported Versions

Our prototype currently supports **JDK 23** for compiling code files.

## Setting Up

To get started, run the following command in the top folder for Unix-like systems (e.g., Linux or macOS):

```bash
./gradlew runWithArgs <codeDir> <skeletonDir> <junitFile> <outputDir>
```

Alternatively, you can manually run the `Main` class with arguments:

```bash
./gradlew run --args="<codeDir> <skeletonDir> <junitFile> <outputDir>"
```

For Windows, use:

```bash
./gradlew.bat runWithArgs <codeDir> <skeletonDir> <junitFile> <outputDir>
```

## What's Left to be Added/Fixed

- API Integration
- User-defined Mark Tables
- File Structure Improvements
- Setting Page and Sidebar Issues in Front-End
- Manual Comments and Reviews

## Acknowledgements

Thanks to my group members for making this a great teamwork experience. Even though this is my first time working on a full-stack project, I am grateful for their support during my exchange semester in Birmingham.

I also pay my respect to the Ainu culture and their elders, their footprints in Hokkaido and the world, in the past, present, and future.

