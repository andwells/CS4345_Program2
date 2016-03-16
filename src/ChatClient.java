//ChatClient;

/**
 * Author: Andrew J. Wells 
 * File Name: ChatClient.java
 * Date: 03/30/2012
 *
 * Description:
 */

/*---GUI-related imports---*/
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

/*---Networking-related imports---*/
import java.io.*;
import java.net.*;
import java.util.List;


public class ChatClient extends JFrame
{
    private JTextArea jepChat;
    private JEditorPane  jepTextToSend;
    private JButton jbtnSend;
    private JPanel bottomPanel;
    private JPanel jplToolBar;
    
    
    /*For performing actions on the text*/
    private Action boldAction;
    private Action italicsAction;
    private Action underlineAction;
    
    static Action sizeAction = new StyledEditorKit.FontSizeAction("Larger", 12);
    private Socket connection;
    
    
    public ChatClient()
    {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setTitle("Chat Client");
        this.setSize(400, 500);
        this.setLayout(new BorderLayout(2,2));
        
        JPanel jplCenter = new JPanel(new GridLayout(2, 1, 0, 50));
        
        jepChat = new JTextArea(8, 20);
        
        jplCenter.add(new JScrollPane(jepChat, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        
        //Builds the panel that goes in the bottom and adds it to the frame
        buildBottomPanel();
        jplCenter.add(bottomPanel, BorderLayout.SOUTH);
        
        this.add(jplCenter);
        
        //Centers the frame and makes it visible
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        try
        {
        	connection = new Socket("127.0.0.1", 50000);
        	new Thread(new backgroundConnection(jepChat, connection)).start();
        	
        	DataOutputStream temp = new DataOutputStream(connection.getOutputStream());
        	temp.writeUTF("test");
        }
        catch(UnknownHostException uhEx)
        {
        	JOptionPane.showMessageDialog(null, "Could not connect to server.");
        }
        catch(IOException ioEX)
        {
        	System.out.println();
        }
        
        
        
        
    }//Closes the constructor
    
    
    private void buildBottomPanel()
    {
        bottomPanel = new JPanel(new BorderLayout(2, 2));
        jplToolBar = new JPanel(new GridLayout(1,5));
        
        jepTextToSend = new JEditorPane();
        jepTextToSend.setSize(new Dimension(200, 200));
        jepTextToSend.setEditorKit(new StyledEditorKit());
        jepTextToSend.setFont(null);
        
        
        
        setupChatKeyBindings();
        buildToolBar();
        
        bottomPanel.add(new JScrollPane(jepTextToSend,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        jbtnSend = new JButton("Send");
        //jbtnSend.addActionListener(new SendListener());
        
        bottomPanel.add(jbtnSend, BorderLayout.EAST);
        bottomPanel.add(jplToolBar, BorderLayout.NORTH);
    }//Closes the method buildBottomPanel
    
    private void buildToolBar()
    {
        JButton jbtnBold = new JButton(boldAction);
        JButton jbtnUnderline = new JButton(underlineAction);
        JButton jbtnItalics = new JButton(italicsAction);
        JSpinner jspnSize = new JSpinner();
        
        jspnSize.setValue(new Integer(12));
        
        
        
        JList   jlstFont = new JList(new String[] {"John", "Jane", "Jim"});
        
        jlstFont.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        jlstFont.setLayoutOrientation(JList.VERTICAL);
        jlstFont.setVisibleRowCount(1);
        JScrollPane listScroller = new JScrollPane(jlstFont);
        listScroller.setPreferredSize(new Dimension(40, 25));
        
        jbtnBold.setText("Bold");
        jbtnUnderline.setText("Underline");
        jbtnItalics.setText("Italics");
        
        
        jplToolBar.add(jbtnBold);
        jplToolBar.add(jbtnUnderline);
        jplToolBar.add(jbtnItalics);
        jplToolBar.add(jspnSize);
        jplToolBar.add(listScroller);
        
        
        
    }//Closes the method buildToolBar
    
    
    
    private void setupChatKeyBindings()
    {
        InputMap inputMap = jepTextToSend.getInputMap();
        
        
        
        boldAction = new StyledEditorKit.BoldAction();
        boldAction.putValue(Action.NAME, "Bold");
        
        
        
        
        
        italicsAction = new StyledEditorKit.ItalicAction();
        boldAction.putValue(Action.NAME, "Italic");
        
        underlineAction = new StyledEditorKit.UnderlineAction();
        boldAction.putValue(Action.NAME, "Underline");
        
        

        KeyStroke boldKey = KeyStroke.getKeyStroke(KeyEvent.VK_B,
                                       Event.CTRL_MASK);
        KeyStroke underlineKey = KeyStroke.getKeyStroke(KeyEvent.VK_U,
                                       Event.CTRL_MASK);
        
        KeyStroke italicsKey = KeyStroke.getKeyStroke(KeyEvent.VK_I,
                                       Event.CTRL_MASK);
        inputMap.put(boldKey, boldAction);
        inputMap.put(italicsKey, italicsAction);
        inputMap.put(underlineKey, underlineAction);
    }
    
    

    
    
    public static void main(String[] args)    
    {
        new ChatClient();
    }//Closes the main method
    
    private class backgroundConnection extends SwingWorker<Object, String>
    {
    	private JTextArea t;
    	private Socket socket;
    	private DataInputStream fromServer;
    	public backgroundConnection(JTextArea writeTo, Socket s)
    	{
    		t = writeTo;
    		socket = s;
    		
    	}

		@Override
		protected Object doInBackground() throws Exception 
		{
			try
			{
				fromServer = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				while(true)
				{
					String output = fromServer.readUTF();
					
					publish(output);
				}
			}
			catch(IOException ioEx)
			{
				JOptionPane.showMessageDialog(null, String.format("The following error occurred: %s\n", ioEx.getMessage()));
			}
			return 1;
		}
			
		@Override
		protected void process(List<String> chunks)
		{
			for(String aChunk : chunks)
			{
				this.t.append(aChunk);
			}
		}
    	
    }
}//Closes the public class ChatClient

