# Predicting Automated Improvement of Software Research

This repository contains a project focused on using machine learning to guide genetic improvement of software by finding regions of source code which are best suited for applying GI edits. The project originally started as a final dissertation project and is now being continued as a self project in my free time. For more information on the original dissertation project as well as my dissertation itself, please visit the [Z7-Final-Dissertation repository](https://github.com/LJSkinner/Z7-Final-Dissertation).

The goal is to see if there is any link between Software Metrics and the editability of a region of source code. I used a mix of existing static code analysis tools such as Checkstyle, and custom metrics that I created using JavaParser. More about those below.

As my dissertation was limited in scope, I was only able to do so much. I tested a number of existing metrics, and created some simple metrics using JavaParser. I tested 4 different types of Machine Learning models, and tuned them (although more time could have been spent on this). The results were not very conclusive and I was unable to produce models with a reasonable amount of accuracy.

It is my hope to continue to investigate and build on what I started, by testing other metric computation tools and creating more involved custom metrics using JavaParser. As well as improving and being more thorough with ML techniques. 

Note that due to the nature of this and it being a research project rather than a standard software project, this repo is very volatile and subject to change. Please do not take this an example of how to do x or y, as it is more intended as place to house this work and as a showcase. I am not expert in any of these fields and I am still learning.

## Genetic Improvement of Software

Genetic Improvement (GI) is a technique used to automatically modify source code with the goal of improving its quality. It uses a process similar to natural selection, where multiple variations of the source code are generated and evaluated for their fitness against some objective function. These variations are then combined, and the process is repeated until a satisfactory result is achieved.

For more information about GI of Software, I recommend having a look at [Genetic Improvement of Software: A Comprehensive Survey](https://ieeexplore.ieee.org/document/7911210) by Petke et al. 

## Software Metrics

Software Metrics are quantitative measures of the attributes of software products or the software process. They are used to identify potential problems in software development and to track progress over time. In this project, we aim to use custom software metrics to identify regions of source code that are most suitable for genetic improvement.

## JavaParser

JavaParser is a Java library used to parse and analyze Java source code. It provides an easy-to-use interface for traversing and manipulating abstract syntax trees (ASTs) generated from Java source code. In this project, we use JavaParser to generate custom software metrics for use in guiding genetic improvement of software. For more information on JavaParser, please visit the [JavaParser repository](https://github.com/javaparser/javaparser).

## The Dataset & Gin 
The dataset was provided by my old dissertation project supervisor [Dr Alexander Brownlee](https://www.linkedin.com/in/sandy-brownlee-85741b3/). This dataset is made up of Genetic Edits which were ran on open-source Java Projects using the GI Tool [Gin](https://github.com/gintool/gin), which has a number of different features and edits that can be performed. Please go check out the repository for more information and show the creators some much deserved love and support.

This dataset was then merged with the metrics computed over the same open-source projects.

## Existing Metrics that have been tested

The following custom software metrics were created using JavaParser:

- Surface Ifs: Counts the number of if statements at the surface level of a method.
- Nested Ifs: Counts the number of nested if statements in a method.
- Surface Fors: Counts the number of for loops at the surface level of a method.
- Nested Fors: Counts the number of nested for loops in a method.
- Surface For Each: Counts the number of for-each loops at the surface level of a method.
- Nested For Each: Counts the number of nested for-each loops in a method.
- Surface While: Counts the number of while loops at the surface level of a method.
- Nested While: Counts the number of nested while loops in a method.
- Surface Do: Counts the number of do-while loops at the surface level of a method.
- Nested Do: Counts the number of nested do-while loops in a method.
- Total Number of Iterative Statements: Counts the total number of iterative statements (i.e. loops) in a method.
- Total Number of Conditional Statements: Counts the total number of conditional statements (i.e. if statements) in a method.

In addition to these custom metrics, we also use CheckStyle, a tool for static code analysis, to calculate metrics like Cyclomatic complexity. For more information on CheckStyle, please visit the [CheckStyle documentation](https://checkstyle.sourceforge.io/).

While the accuracy of the models using these metrics wasn't the best, they provided a starting point for investigating other metrics that could potentially guide genetic improvement of software. If you're interested in the other Checkstyle Metrics that were used, please check out the original repo. 

## More custom metrics with JavaParser
There are a few metrics that I wanted to create with JavaParser and test that I never got around to during my dissertation. So that will be my starting point.

These metrics are:
- Num Method Calls: A variation of Fan In which counts the number of other methods that are called in the target method.
- Num Local Variables: Counts the number of local variables in the method
- Number of Code Duplication Occurences: Counts the number of times code is duplicated.
- Number of Logical Operators: Counts the number of logical operators in the method.
- Number of Comparison Operators: Counts the number of comparison operators in the method.

## Respository Structure
At the moment the repo just contains the source code for the JavaParser Metric Computer that is written in Java. However, once I have fixed the formatting I will add the JupyterNote files that contain all my Machine Learning work. This used the Jupyter Stack, including Scikit-Learn. Please check out their [site](https://scikit-learn.org/) for more information.

## License
This project is licensed under MIT. Please see the [LICENSE.md](LICENSE.md) file for more details.
