/**
 * Copyright University of Stirling 2021-2022
 */
package com.ljskinner.jputils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.NoSuchElementException;

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
	 * <p>
	 * You can use the helper method provided to obtain a Method Declaration node, this should
	 * work on all fully formed method names.
	 *
	 * @param methodDeclaration The method declaration node that represents the method to calculate this metric from.
	 *
	 * @see #findMethodDeclarationNode(String)
	 *
	 * 
	 * @return The number of statements matching the if keyword.
	 */
	public int numberOfAllIfIn(MethodDeclaration methodDeclaration) {
		int numIfs = 0;

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
	 * <p>
	 * You can use the helper method provided to obtain a Method Declaration node, this should
	 * work on all fully formed method names.
	 *
	 * @param methodDeclaration The method declaration node that represents the method to calculate this metric from.
	 *
	 * @see #findMethodDeclarationNode(String)
	 *
	 * @throws NoSuchElementException If the parent node or node above the statement doesn't have a value.
	 *                                This should only really ever happen if an empty MethodDeclaration node
	 *                                is passed to the method.
	 *
	 * @return The number of nested Ifs contained in the source file
	 */
	public int numberOfNestedIfIn(MethodDeclaration methodDeclaration) {
		int numNestedIfs = 0;

		List<IfStmt> ifStmts = methodDeclaration.findAll(IfStmt.class);

		for (IfStmt ifStmt : ifStmts) {
			Node parent = ifStmt.getParentNode().orElseThrow();

			// JavaParser treats else statements as IfStmts, I.E there is no ElseStmt class.
			// We want to ignore the paths for the sake of this metric.
			if(parent instanceof IfStmt) {
				continue;
			}

			Node nodeAboveIf = parent.getParentNode().orElseThrow();

			// We say a statement is nested if the node above it is not the method declaration
			if (!(nodeAboveIf instanceof MethodDeclaration)) {
				numNestedIfs++;
			}

		}

		return numNestedIfs;
	}

	/**
	 * This method will return the number of surface ifs which are contained within
	 * in a given method which is contained within the source file.
	 * <p>
	 * You can use the helper method provided to obtain a Method Declaration node, this should
	 * work on all fully formed method names.
	 *
	 * @param methodDeclaration The method declaration node that represents the method to calculate this metric from.
	 *
	 * @see #findMethodDeclarationNode(String)
	 *
	 * @throws NoSuchElementException If the parent node or node above the statement doesn't have a value.
	 *                                This should only really ever happen if an empty MethodDeclaration node
	 *                                is passed to the method.
	 *
	 * @return The number of surface Ifs contained in the source file
	 */
	public int numberOfSurfaceIfIn(MethodDeclaration methodDeclaration) {
		int numberSurfaceIfs = 0;

		List<IfStmt> ifStmts = methodDeclaration.findAll(IfStmt.class);

		for (IfStmt ifStmt : ifStmts) {
			Node parent = ifStmt.getParentNode().orElseThrow();

			Node nodeAboveIf = parent.getParentNode().orElseThrow();

			if (nodeAboveIf instanceof MethodDeclaration) {
				numberSurfaceIfs++;
			}
		}

		return numberSurfaceIfs;
	}

	/**
	 * This method will return the number of nested switches which are contained
	 * within in a given method which is contained within the source file.
	 * <p>
	 * You can use the helper method provided to obtain a Method Declaration node, this should
	 * work on all fully formed method names.
	 *
	 * @param methodDeclaration The method declaration node that represents the method to calculate this metric from.
	 *
	 * @see #findMethodDeclarationNode(String)
	 *
	 * @throws NoSuchElementException If the parent node or node above the statement doesn't have a value.
	 *                                This should only really ever happen if an empty MethodDeclaration node
	 *                                is passed to the method.
	 *
	 * @return The number of nested switches contained in the source file
	 */
	public int numberOfNestedSwitchIn(MethodDeclaration methodDeclaration) {
		int numNestedSwitches = 0;

		List<SwitchStmt> switchStmts = methodDeclaration.findAll(SwitchStmt.class);

		for (SwitchStmt switchStmt : switchStmts) {
			Node parent = switchStmt.getParentNode().orElseThrow();

			Node nodeAboveSwitch = parent.getParentNode().orElseThrow();

			// We say a statement is nested if the node above it is not the method declaration
			if (!(nodeAboveSwitch instanceof MethodDeclaration)) {
				numNestedSwitches++;
			}
		}

		return numNestedSwitches;
	}

	/**
	 * This method will return the number of surface switches which are contained
	 * within in a given method which is contained within the source file.
	 * <p>
	 * You can use the helper method provided to obtain a Method Declaration node, this should
	 * work on all fully formed method names.
	 *
	 * @param methodDeclaration The method declaration node that represents the method to calculate this metric from.
	 *
	 * @see #findMethodDeclarationNode(String)
	 *
	 * @throws NoSuchElementException If the parent node or node above the statement doesn't have a value.
	 *                                This should only really ever happen if an empty MethodDeclaration node
	 *                                is passed to the method.
	 *
	 * @return The number of surface switches contained in the source file
	 */
	public int numberOfSurfaceSwitchIn(MethodDeclaration methodDeclaration) {
		int numberSurfaceSwitches = 0;

		List<SwitchStmt> switchStmts = methodDeclaration.findAll(SwitchStmt.class);

		for (SwitchStmt switchStmt : switchStmts) {
			Node parent = switchStmt.getParentNode().orElseThrow();

			Node nodeAboveSwitch = parent.getParentNode().orElseThrow();

			if (nodeAboveSwitch instanceof MethodDeclaration) {
				numberSurfaceSwitches++;
			}
		}

		return numberSurfaceSwitches;
	}

	/**
	 * This method will return the number of nested whiles which are contained
	 * within in a given method which is contained within the source file.
	 * <p>
	 * You can use the helper method provided to obtain a Method Declaration node, this should
	 * work on all fully formed method names.
	 *
	 * @param methodDeclaration The method declaration node that represents the method to calculate this metric from.
	 *
	 * @see #findMethodDeclarationNode(String)
	 *
	 * @throws NoSuchElementException If the parent node or node above the statement doesn't have a value.
	 *                                This should only really ever happen if an empty MethodDeclaration node
	 *                                is passed to the method.
	 *
	 * @return The number of nested whiles contained in the source file
	 */
	public int numberOfNestedWhileIn(MethodDeclaration methodDeclaration) {
		int numNestedWhiles = 0;

		List<WhileStmt> whileStmts = methodDeclaration.findAll(WhileStmt.class);

		for (WhileStmt whileStmt : whileStmts) {
			Node parent = whileStmt.getParentNode().orElseThrow();

			Node nodeAboveWhile = parent.getParentNode().orElseThrow();

			// We say a statement is nested if the node above it is not the method declaration
			if (!(nodeAboveWhile instanceof MethodDeclaration)) {
				numNestedWhiles++;
			}
		}

		return numNestedWhiles;
	}

	/**
	 * This method will return the number of surface whiles which are contained
	 * within in a given method which is contained within the source file.
	 * <p>
	 * You can use the helper method provided to obtain a Method Declaration node, this should
	 * work on all fully formed method names.
	 *
	 * @param methodDeclaration The method declaration node that represents the method to calculate this metric from.
	 *
	 * @see #findMethodDeclarationNode(String)
	 *
	 * @throws NoSuchElementException If the parent node or node above the statement doesn't have a value.
	 *                                This should only really ever happen if an empty MethodDeclaration node
	 *                                is passed to the method.
	 *
	 * @return The number of surface whiles contained in the source file
	 */
	public int numberOfSurfaceWhileIn(MethodDeclaration methodDeclaration) {
		int numberSurfaceWhiles = 0;

		List<WhileStmt> whileStmts = methodDeclaration.findAll(WhileStmt.class);

		for (WhileStmt whileStmt : whileStmts) {
			Node parent = whileStmt.getParentNode().orElseThrow();

			Node nodeAboveWhile = parent.getParentNode().orElseThrow();

			if (nodeAboveWhile instanceof MethodDeclaration) {
				numberSurfaceWhiles++;
			}
		}

		return numberSurfaceWhiles;
	}

	/**
	 * This method will return the number of nested dos which are contained within
	 * in a given method which is contained within the source file.
	 * <p>
	 * You can use the helper method provided to obtain a Method Declaration node, this should
	 * work on all fully formed method names.
	 *
	 * @param methodDeclaration The method declaration node that represents the method to calculate this metric from.
	 *
	 * @see #findMethodDeclarationNode(String)
	 *
	 * @throws NoSuchElementException If the parent node or node above the statement doesn't have a value.
	 *                                This should only really ever happen if an empty MethodDeclaration node
	 *                                is passed to the method.
	 *
	 * @return The number of nested dos contained in the source file
	 */
	public int numberOfNestedDoIn(MethodDeclaration methodDeclaration) {
		int numNestedDos = 0;

		List<DoStmt> doStmts = methodDeclaration.findAll(DoStmt.class);

		for (DoStmt doStmt : doStmts) {
			Node parent = doStmt.getParentNode().orElseThrow();

			Node nodeAboveDo = parent.getParentNode().orElseThrow();

			// We say a statement is nested if the node above it is not the method declaration
			if (!(nodeAboveDo instanceof MethodDeclaration)) {
				numNestedDos++;
			}
		}

		return numNestedDos;
	}

	/**
	 * This method will return the number of surface dos which are contained within
	 * in a given method which is contained within the source file.
	 * <p>
	 * You can use the helper method provided to obtain a Method Declaration node, this should
	 * work on all fully formed method names.
	 *
	 * @param methodDeclaration The method declaration node that represents the method to calculate this metric from.
	 *
	 * @see #findMethodDeclarationNode(String)
	 *
	 * @throws NoSuchElementException If the parent node or node above the statement doesn't have a value.
	 *                                This should only really ever happen if an empty MethodDeclaration node
	 *                                is passed to the method.
	 *
	 * @return The number of surface dos contained in the source file
	 */
	public int numberOfSurfaceDoIn(MethodDeclaration methodDeclaration) {
		int numberSurfaceDos = 0;

		List<DoStmt> doStmts = methodDeclaration.findAll(DoStmt.class);

		for (DoStmt doStmt : doStmts) {
			Node parent = doStmt.getParentNode().orElseThrow();

			Node nodeAboveDo = parent.getParentNode().orElseThrow();

			if (nodeAboveDo instanceof MethodDeclaration) {
				numberSurfaceDos++;
			}
		}

		return numberSurfaceDos;
	}

	/**
	 * This method will return the number of nested fors which are contained within
	 * in a given method which is contained within the source file.
	 * <p>
	 * You can use the helper method provided to obtain a Method Declaration node, this should
	 * work on all fully formed method names.
	 *
	 * @param methodDeclaration The method declaration node that represents the method to calculate this metric from.
	 *
	 * @see #findMethodDeclarationNode(String)
	 *
	 * @throws NoSuchElementException If the parent node or node above the statement doesn't have a value.
	 *                                This should only really ever happen if an empty MethodDeclaration node
	 *                                is passed to the method.
	 *
	 * @return The number of nested fors contained in the source file
	 */
	public int numberOfNestedForIn(MethodDeclaration methodDeclaration) {
		int numNestedFors = 0;

		List<ForStmt> forStmts = methodDeclaration.findAll(ForStmt.class);

		for (ForStmt forStmt : forStmts) {
			Node parent = forStmt.getParentNode().orElseThrow();

			Node nodeAboveFor = parent.getParentNode().orElseThrow();

			// We say a statement is nested if the node above it is not the method declaration
			if (!(nodeAboveFor instanceof MethodDeclaration)) {
				numNestedFors++;
			}
		}

		return numNestedFors;
	}

	/**
	 * This method will return the number of surface fors which are contained within
	 * in a given method which is contained within the source file.
	 * <p>
	 * You can use the helper method provided to obtain a Method Declaration node, this should
	 * work on all fully formed method names.
	 *
	 * @param methodDeclaration The method declaration node that represents the method to calculate this metric from.
	 *
	 * @see #findMethodDeclarationNode(String)
	 *
	 * @throws NoSuchElementException If the parent node or node above the statement doesn't have a value.
	 *                                This should only really ever happen if an empty MethodDeclaration node
	 *                                is passed to the method.
	 *
	 * @return The number of surface fors contained in the source file
	 */
	public int numberOfSurfaceForIn(MethodDeclaration methodDeclaration) {
		int numberSurfaceFors = 0;

		List<ForStmt> forStmts = methodDeclaration.findAll(ForStmt.class);

		for (ForStmt forStmt : forStmts) {
			Node parent = forStmt.getParentNode().orElseThrow();

			Node nodeAboveFor = parent.getParentNode().orElseThrow();

			if (nodeAboveFor instanceof MethodDeclaration) {
				numberSurfaceFors++;
			}
		}

		return numberSurfaceFors;
	}

	/**
	 * This method will return the number of nested for eaches which are contained
	 * within in a given method which is contained within the source file.
	 * <p>
	 * You can use the helper method provided to obtain a Method Declaration node, this should
	 * work on all fully formed method names.
	 *
	 * @param methodDeclaration The method declaration node that represents the method to calculate this metric from.
	 *
	 * @see #findMethodDeclarationNode(String)
	 *
	 * @return The number of nested for eaches contained in the source file
	 */
	public int numberOfNestedForEachIn(MethodDeclaration methodDeclaration) {
		int numNestedForEach = 0;

		List<ForEachStmt> forEachStmts = methodDeclaration.findAll(ForEachStmt.class);

		for (ForEachStmt forEachStmt : forEachStmts) {
			Node parent = forEachStmt.getParentNode().orElseThrow();

			Node nodeAboveForEach = parent.getParentNode().orElseThrow();

			// We say a statement is nested if the node above it is not the method declaration
			if (!(nodeAboveForEach instanceof MethodDeclaration)) {
				numNestedForEach++;
			}
		}

		return numNestedForEach;

	}

	/**
	 * This method will return the number of surface for eaches which are contained
	 * within in a given method which is contained within the source file.
	 * <p>
	 * You can use the helper method provided to obtain a Method Declaration node, this should
	 * work on all fully formed method names.
	 *
	 * @param methodDeclaration The method declaration node that represents the method to calculate this metric from.
	 *
	 * @see #findMethodDeclarationNode(String)
	 *
	 * @throws NoSuchElementException If the parent node or node above the statement doesn't have a value.
	 *                                This should only really ever happen if an empty MethodDeclaration node
	 *                                is passed to the method.
	 *
	 * @return The number of surface for eaches contained in the source file
	 */
	public int numberOfSurfaceForEachIn(MethodDeclaration methodDeclaration) {
		int numberSurfaceForEach = 0;

		List<ForEachStmt> forEachStmts = methodDeclaration.findAll(ForEachStmt.class);

		for (ForEachStmt forEachStmt : forEachStmts) {
			Node parent = forEachStmt.getParentNode().orElseThrow();

			Node nodeAboveForEach = parent.getParentNode().orElseThrow();

			if (nodeAboveForEach instanceof MethodDeclaration) {
				numberSurfaceForEach++;
			}
		}

		return numberSurfaceForEach;
	}

	/**
	 * This method will return the number of method calls inside the provided method. Note
	 * that this does not differentiate between recursive and other function calls. It is the total.
	 * <p>
	 * You can use the helper method provided to obtain a Method Declaration node, this should
	 * work on all fully formed method names.
	 *
	 * @param methodDeclaration The method declaration node that represents the method to calculate this metric from.
	 *
	 * @see #findMethodDeclarationNode(String)
	 *
	 * @return The number of total method calls in the provided method
	 */
	public int numberOfMethodCallsIn(MethodDeclaration methodDeclaration) {
		return methodDeclaration.findAll(MethodCallExpr.class).size();
	}

	/**
	 * This method will return the number of local variables inside the provided method.
	 * <p>
	 * You can use the helper method provided to obtain a Method Declaration node, this should
	 * work on all fully formed method names.
	 *
	 * @param methodDeclaration The method declaration node that represents the method to calculate this metric from.
	 *
	 * @see #findMethodDeclarationNode(String)
	 *
	 * @return The number of local variables in the provided method
	 */
	public int numberOfLocalVariablesIn(MethodDeclaration methodDeclaration) {
		return methodDeclaration.findAll(VariableDeclarationExpr.class).size();
	}

	/**
	 * This method will return the number of logical operators in the provided method.
	 * This will count &&, ||, &, | and ^.
	 * <p>
	 * You can use the helper method provided to obtain a Method Declaration node, this should
	 * work on all fully formed method names.
	 *
	 * @param methodDeclaration The method declaration node that represents the method to calculate this metric from.
	 *
	 * @see #findMethodDeclarationNode(String)
	 *
	 * @return The number of logical operators (&&, ||, &, |, ^) in the provided method
	 */
	public int numberOfLogicalOperatorsIn(MethodDeclaration methodDeclaration) {
		int numLogicalOperators = 0;

		List<BinaryExpr> binaryExprs = methodDeclaration.findAll(BinaryExpr.class);

		for (BinaryExpr binaryExpr : binaryExprs) {
			boolean isLogicalOperator = binaryExpr.getOperator() == BinaryExpr.Operator.AND ||
					                    binaryExpr.getOperator() == BinaryExpr.Operator.OR ||
					                    binaryExpr.getOperator() == BinaryExpr.Operator.XOR ||
					                    binaryExpr.getOperator() == BinaryExpr.Operator.BINARY_AND ||
					                    binaryExpr.getOperator() == BinaryExpr.Operator.BINARY_OR;
			if (isLogicalOperator) {
				numLogicalOperators++;
			}
		}

		return numLogicalOperators;
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
	public MethodDeclaration findMethodDeclarationNode(String methodName) {
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
