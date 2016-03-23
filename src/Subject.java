/**
 * Author: Andrew J. Wells 
 * File Name: Subject.java
 * Date: 03/12/2016
 *
 * Description: The Observer in the Observer pattern
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
