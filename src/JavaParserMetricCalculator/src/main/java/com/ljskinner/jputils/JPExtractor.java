/**
 * Copyright University of Stirling 2021-2022
 */
package com.ljskinner.jputils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Objects;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

/**
 * This class is a wrapper for JavaParser and is responsible for the main work
 * in extracting metrics and other details from a Java Source file, which is
 * known a "CompilationUnit". The methodology behind this is that while
 * primarily being used to extract simple metrics from a Java Source file, it
 * will also contain methods to easily retrieve common information; such as all
 * methods contained in the file.
 * <p>
 * This was built to work with Java 17 and JavaParser 3.24.0.
 * <p>
 * For more details about JavaParser, please see the linked documentation below,
 * all rights for JavaParser go to their original authors.
 *
 * @see <a href="https://github.com/javaparser/javaparser">JavaParser Github Repo</a>
 * @see <a href="https://javaparser.org/">JavaParser Website</a>
 *
 * @author Luke Skinner
 *
 */
public class JPExtractor {

	/**
	 * This is responsible for storing the CompilationUnit which will be used as the
	 * root node, representing the Java Source File.
	 */
	private CompilationUnit compilationUnit;

	/**
	 * This creates the state of an extractor using an existing CompiliationUnit. If
	 * you have already used JavaParser to create a CompiliationUnit elsewhere, then
	 * this constructor allows you to use that unit.
	 * 
	 * @param compilationUnit The Compilation Unit to use as the root node for this
	 *                        extractor
	 * 
	 * @throws NullPointerException If the compilation unit which is supplied is
	 *                              null
	 */
	public JPExtractor(CompilationUnit compilationUnit) {
		Objects.requireNonNull(compilationUnit,
				"The provided compilation unit cannot be null, please make sure you have created the unit correctly and try again!");

		this.compilationUnit = compilationUnit;
	}

	/**
	 * This creates the state of an extractor using a file path of a java source
	 * file which will be parsed by JavaParser to create a CompilationUnit
	 * representing it.
	 * 
	 * @param filePath The file path to be used by JavaParser to create a
	 *                 CompiliationUnit.
	 * 
	 * @throws FileNotFoundException    If the file path that is provided does not
	 *                                  exist
	 * @throws NullPointerException     If the file path that is provided is null
	 * @throws IllegalArgumentException If the file path provided is empty
	 */
	public JPExtractor(String filePath) throws FileNotFoundException {
		Objects.requireNonNull(filePath,
				"The supplied file path cannot be null, please make sure you have provided a valid path!");

		filePath = filePath.trim();

		if (filePath.isEmpty()) {
			throw new IllegalArgumentException(
					"The supplied file path cannot be empty, please make sure you have provided a valid path!");
		}

		this.compilationUnit = StaticJavaParser.parse(new File(filePath));
	}

	/**
	 * This method will compute the number of all statements which match the if
	 * keyword. This will include "else" statements.
	 * 
	 * @return The number of statements matching the if keyword.
	 */
	public int numberOfAllIfIn(String methodName) {
		int numIfs = 0;

		MethodDeclaration methodDeclaration = findMethodDeclarationNode(methodName);

		List<IfStmt> ifStatements = methodDeclaration.findAll(IfStmt.class);

		for (IfStmt ifStmt : ifStatements) {
			numIfs++;

			boolean hasElse = ifStmt.getElseStmt().isPresent();

			if (hasElse) {
				Statement elseStmt = ifStmt.getElseStmt().get();

				boolean isElseIf = elseStmt instanceof IfStmt;

				// We already count else ifs, so if it is not an "else-if" block then it must be
				// an "else" block (which is not accounted for by default)
				if (!isElseIf) {
					numIfs++;
				}
			}
		}

		return numIfs;
	}

	/**
	 * This method will return the number of nested ifs which are contained within
	 * in a given method which is contained within the source file.
	 * 
	 * @param methodName
	 * 
	 * @return The number of nested Ifs contained in the source file
	 */
	public int numberOfNestedIfIn(String methodName) {
		int numNestedIfs = 0;

		MethodDeclaration methodDeclaration = findMethodDeclarationNode(methodName);

		List<Statement> statements = methodDeclaration.findAll(Statement.class);

		for (Statement statement : statements) {
			List<Node> children = statement.getChildNodes();

			for (Node child : children) {
				if (child instanceof BlockStmt) {
					List<Node> blockChildren = child.getChildNodes();

					for (Node blockChild : blockChildren) {

						if (blockChild instanceof IfStmt) {
							numNestedIfs++;
						}
					}
				}

			}
		}

		return numNestedIfs;

	}

	/**
	 * This method will return the number of surface ifs which are contained within
	 * in a given method which is contained within the source file.
	 * 
	 * @param methodName
	 * 
	 * @return The number of surface Ifs contained in the source file
	 */
	public int numberOfSurfaceIfIn(String methodName) {
		int numberSurfaceIfs = 0;

		MethodDeclaration methodDeclaration = findMethodDeclarationNode(methodName);

		List<Statement> statements = methodDeclaration.findAll(Statement.class);


		for (Statement statement : statements) {
			if (statement instanceof IfStmt) {

				IfStmt ifStmt = (IfStmt) statement;

				Node parentOfIf = ifStmt.getParentNode().get();

				if (parentOfIf instanceof BlockStmt) {
					Node parentOfBlock = parentOfIf.getParentNode().get();

					if (parentOfBlock instanceof MethodDeclaration) {
						numberSurfaceIfs++;
					}
				}

			}
		}

		return numberSurfaceIfs;

	}

	/**
	 * This method will return the number of nested switches which are contained
	 * within in a given method which is contained within the source file.
	 * 
	 * @param methodName
	 * 
	 * @return The number of nested switches contained in the source file
	 */
	public int numberOfNestedSwitchIn(String methodName) {
		int numNestedSwitches = 0;

		MethodDeclaration methodDeclaration = findMethodDeclarationNode(methodName);

		List<Statement> statements = methodDeclaration.findAll(Statement.class);

		for (Statement statement : statements) {
			List<Node> children = statement.getChildNodes();

			for (Node child : children) {
				if (child instanceof BlockStmt) {
					List<Node> blockChildren = child.getChildNodes();

					for (Node blockChild : blockChildren) {

						if (blockChild instanceof SwitchStmt) {
							numNestedSwitches++;
						}
					}
				}

			}
		}

		return numNestedSwitches;
	}

	/**
	 * This method will return the number of surface switches which are contained
	 * within in a given method which is contained within the source file.
	 * 
	 * @param methodName
	 * 
	 * @return The number of surface switches contained in the source file
	 */
	public int numberOfSurfaceSwitchIn(String methodName) {
		int numberSurfaceSwitches = 0;

		MethodDeclaration methodDeclaration = findMethodDeclarationNode(methodName);

		List<Statement> statements = methodDeclaration.findAll(Statement.class);

		for (Statement statement : statements) {
			if (statement instanceof SwitchStmt) {

				SwitchStmt switchStmt = (SwitchStmt) statement;

				Node parentOfSwitch = switchStmt.getParentNode().get();

				if (parentOfSwitch instanceof BlockStmt) {
					Node parentOfBlock = parentOfSwitch.getParentNode().get();

					if (parentOfBlock instanceof MethodDeclaration) {
						numberSurfaceSwitches++;
					}
				}

			}
		}

		return numberSurfaceSwitches;
	}

	/**
	 * This method will return the number of nested whiles which are contained
	 * within in a given method which is contained within the source file.
	 * 
	 * @param methodName
	 * 
	 * @return The number of nested whiles contained in the source file
	 */
	public int numberOfNestedWhileIn(String methodName) {
		int numNestedWhiles = 0;

		MethodDeclaration methodDeclaration = findMethodDeclarationNode(methodName);

		List<Statement> statements = methodDeclaration.findAll(Statement.class);

		for (Statement statement : statements) {
			List<Node> children = statement.getChildNodes();

			for (Node child : children) {
				if (child instanceof BlockStmt) {
					List<Node> blockChildren = child.getChildNodes();

					for (Node blockChild : blockChildren) {

						if (blockChild instanceof WhileStmt) {
							numNestedWhiles++;
						}
					}
				}

			}
		}

		return numNestedWhiles;
	}

	/**
	 * This method will return the number of nested dos which are contained within
	 * in a given method which is contained within the source file.
	 * 
	 * @param methodName
	 * 
	 * @return The number of nested dos contained in the source file
	 */
	public int numberOfNestedDoIn(String methodName) {
		int numNestedDos = 0;

		MethodDeclaration methodDeclaration = findMethodDeclarationNode(methodName);

		List<Statement> statements = methodDeclaration.findAll(Statement.class);

		for (Statement statement : statements) {
			List<Node> children = statement.getChildNodes();

			for (Node child : children) {
				if (child instanceof BlockStmt) {
					List<Node> blockChildren = child.getChildNodes();

					for (Node blockChild : blockChildren) {

						if (blockChild instanceof DoStmt) {
							numNestedDos++;
						}
					}
				}

			}
		}

		return numNestedDos;
	}

	/**
	 * This method will return the number of surface dos which are contained within
	 * in a given method which is contained within the source file.
	 * 
	 * @param methodName
	 * 
	 * @return The number of surface dos contained in the source file
	 */
	public int numberOfSurfaceDoIn(String methodName) {
		int numberSurfaceDos = 0;

		MethodDeclaration methodDeclaration = findMethodDeclarationNode(methodName);

		List<Statement> statements = methodDeclaration.findAll(Statement.class);

		for (Statement statement : statements) {
			if (statement instanceof DoStmt) {

				DoStmt doStmt = (DoStmt) statement;

				Node parentOfDo = doStmt.getParentNode().get();

				if (parentOfDo instanceof BlockStmt) {
					Node parentOfBlock = parentOfDo.getParentNode().get();

					if (parentOfBlock instanceof MethodDeclaration) {
						numberSurfaceDos++;
					}
				}

			}
		}

		return numberSurfaceDos;
	}

	/**
	 * This method will return the number of surface whiles which are contained
	 * within in a given method which is contained within the source file.
	 * 
	 * @param methodName
	 * 
	 * @return The number of surface whiles contained in the source file
	 */
	public int numberOfSurfaceWhileIn(String methodName) {
		int numberSurfaceWhiles = 0;

		MethodDeclaration methodDeclaration = findMethodDeclarationNode(methodName);

		List<Statement> statements = methodDeclaration.findAll(Statement.class);

		for (Statement statement : statements) {
			if (statement instanceof WhileStmt) {

				WhileStmt whileStmt = (WhileStmt) statement;

				Node parentOfWhile = whileStmt.getParentNode().get();

				if (parentOfWhile instanceof BlockStmt) {
					Node parentOfBlock = parentOfWhile.getParentNode().get();

					if (parentOfBlock instanceof MethodDeclaration) {
						numberSurfaceWhiles++;
					}
				}

			}
		}

		return numberSurfaceWhiles;
	}

	/**
	 * This method will return the number of nested fors which are contained within
	 * in a given method which is contained within the source file.
	 * 
	 * @param methodName
	 * 
	 * @return The number of nested fors contained in the source file
	 */
	public int numberOfNestedForIn(String methodName) {
		int numNestedFors = 0;

		MethodDeclaration methodDeclaration = findMethodDeclarationNode(methodName);

		List<Statement> statements = methodDeclaration.findAll(Statement.class);

		for (Statement statement : statements) {
			List<Node> children = statement.getChildNodes();

			for (Node child : children) {
				if (child instanceof BlockStmt) {
					List<Node> blockChildren = child.getChildNodes();

					for (Node blockChild : blockChildren) {

						if (blockChild instanceof ForStmt) {
							numNestedFors++;
						}
					}
				}

			}
		}

		return numNestedFors;
	}

	/**
	 * This method will return the number of nested for eaches which are contained
	 * within in a given method which is contained within the source file.
	 * 
	 * @param methodName
	 * 
	 * @return The number of nested for eaches contained in the source file
	 */
	public int numberOfNestedForEachIn(String methodName) {
		int numNestedForEach = 0;

		MethodDeclaration methodDeclaration = findMethodDeclarationNode(methodName);

		List<Statement> statements = methodDeclaration.findAll(Statement.class);

		for (Statement statement : statements) {
			List<Node> children = statement.getChildNodes();

			for (Node child : children) {
				if (child instanceof BlockStmt) {
					List<Node> blockChildren = child.getChildNodes();

					for (Node blockChild : blockChildren) {

						if (blockChild instanceof ForEachStmt) {
							numNestedForEach++;
						}
					}
				}

			}
		}

		return numNestedForEach;

	}

	/**
	 * This method will return the number of surface for eaches which are contained
	 * within in a given method which is contained within the source file.
	 * 
	 * @param methodName
	 * 
	 * @return The number of surface for eaches contained in the source file
	 */
	public int numberOfSurfaceForEachIn(String methodName) {
		int numberSurfaceForEach = 0;

		MethodDeclaration methodDeclaration = findMethodDeclarationNode(methodName);

		List<Statement> statements = methodDeclaration.findAll(Statement.class);

		for (Statement statement : statements) {
			if (statement instanceof ForEachStmt) {

				ForEachStmt forEachStmt = (ForEachStmt) statement;

				Node parentOfForEach = forEachStmt.getParentNode().get();

				if (parentOfForEach instanceof BlockStmt) {
					Node parentOfBlock = parentOfForEach.getParentNode().get();

					if (parentOfBlock instanceof MethodDeclaration) {
						numberSurfaceForEach++;
					}
				}

			}
		}

		return numberSurfaceForEach;
	}

	/**
	 * This method will return the number of surface fors which are contained within
	 * in a given method which is contained within the source file.
	 * 
	 * @param methodName
	 * 
	 * @return The number of surface fors contained in the source file
	 */
	public int numberOfSurfaceForIn(String methodName) {
		int numberSurfaceFors = 0;

		MethodDeclaration methodDeclaration = findMethodDeclarationNode(methodName);

		List<Statement> statements = methodDeclaration.findAll(Statement.class);

		for (Statement statement : statements) {
			if (statement instanceof ForStmt) {

				ForStmt forStmt = (ForStmt) statement;

				Node parentOfFor = forStmt.getParentNode().get();

				if (parentOfFor instanceof BlockStmt) {
					Node parentOfBlock = parentOfFor.getParentNode().get();

					if (parentOfBlock instanceof MethodDeclaration) {
						numberSurfaceFors++;
					}
				}

			}
		}

		return numberSurfaceFors;
	}

	/**
	 * 
	 * This method will return the number of all which match the specified type, so
	 * long as it extends JavaParser's Node class.
	 * 
	 * This will strictly match on the type, if you are looking for a more
	 * personalised use case. There are alternative methods for counting more
	 * specifically.
	 * 
	 * @param <T>  Any class which extends JavaParser's Node.
	 * 
	 * @param type The type to search for in the AST
	 * 
	 * @return The number of statements matching the specified type
	 */
	public <T extends Node> int numberOfAllByType(Class<T> type) {
		return compilationUnit.findAll(type).size();
	}

	/**
	 * Retrieve the current compilation unit
	 * 
	 * @return compilationUnit The current compilation unit which is being used as
	 *         the root node
	 */
	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	/**
	 * Sets the extractor using an existing CompiliationUnit.
	 * 
	 * @param compilationUnit The Compilation Unit to use as the root node for this
	 *                        extractor
	 * 
	 * @throws NullPointerException If the compilation unit which is supplied is
	 *                              null
	 */
	public void setCompilationUnit(CompilationUnit compilationUnit) {
		Objects.requireNonNull(compilationUnit,
				"The provided compilation unit cannot be null, please make sure you have created the unit correctly and try again!");

		this.compilationUnit = compilationUnit;
	}

	/**
	 * Sets the extractor using a file path of a java source file which will be
	 * parsed by JavaParser to create a CompilationUnit representing it.
	 * 
	 * @param filePath The file path to be used by JavaParser to create a
	 *                 CompiliationUnit.
	 * 
	 * @throws FileNotFoundException    If the file path that is provided does not
	 *                                  exist
	 * @throws NullPointerException     If the file path that is provided is null
	 * @throws IllegalArgumentException If the file path provided is empty
	 */
	public void setCompilationUnit(String filePath) throws FileNotFoundException {
		Objects.requireNonNull(filePath,
				"The supplied file path cannot be null, please make sure you have provided a valid path!");

		filePath = filePath.trim();

		if (filePath.isEmpty()) {
			throw new IllegalArgumentException(
					"The supplied file path cannot be empty, please make sure you have provided a valid path!");
		}

		this.compilationUnit = StaticJavaParser.parse(new File(filePath));
	}

	/**
	 * This method is responsible for returning an indication of whether or not the
	 * current source file has a method matching the method name supplied.
	 * 
	 * @param methodName The method name to check
	 * 
	 * @return {@code true} If the current source file has a method matching the
	 *         method name, {@code false} otherwise.
	 */
	public boolean hasMethod(String methodName) {
		for (MethodDeclaration md : compilationUnit.findAll(MethodDeclaration.class)) {

			// We will break the declaration into parts, first we want to ignore spaces
			// after a comma. Then we can split on the remaining spaces
			String[] parts = md.getDeclarationAsString(false, false, false).replace(", ", ",").split(" ");

			// We can then look at the last element which will be the name and the param
			// list
			String declarationString = parts[parts.length - 1];

			// Then match the method name passed
			if (methodName.contains(declarationString)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * This method is responsible for finding the method declaration node which
	 * matches the method name provided. If there is no method declaration node in
	 * the compilation unit (source file) which matches the method name provided,
	 * then this will return an empty method declaration node.
	 * 
	 * @param methodName The method name to check
	 * 
	 * @return The method declaration node which matches the method name provided,
	 *         or empty if there is not one.
	 */
	private MethodDeclaration findMethodDeclarationNode(String methodName) {
		MethodDeclaration targetMethodDeclaration = new MethodDeclaration();

		for (MethodDeclaration md : compilationUnit.findAll(MethodDeclaration.class)) {

			// We will break the declaration into parts, first we want to ignore spaces
			// after a comma. Then we can split on the remaining spaces
			String[] parts = md.getDeclarationAsString(false, false, false).replace(", ", ",").split(" ");

			// We can then look at the last element which will be the name and the param
			// list
			String declarationString = parts[parts.length - 1];

			// Then match the method name passed
			if (methodName.contains(declarationString)) {
				targetMethodDeclaration = md;
			}
		}

		return targetMethodDeclaration;
	}

}
