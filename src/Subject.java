/**
 * Author: Max Carter, Robert Walker, Andrew Wells
 * Course Number: CS4345
 * Semester: Spring 2016
 * Assignment: Program2
 * File Name: Subject.java
 * Date: 03/12/2016
 *
 * Description: The Subject in the Observer pattern
 */



public interface Subject
{
	//Adds observer passed in to list of observing clients
	boolean registerObserver(Observer o);
	
	//Removes observer passed in
	void removeObserver(Observer o);
	
	//Performs push update
	void notifyObservers(Object o);
	
	//add method for pull
}
