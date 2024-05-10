package Mukuro.TodoApp;
import javax.swing.*;
import java.awt.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import org.sqlite.SQLiteDataSource;
public class TodoAppApplication {

	private static final String DATABASE_URL = "jdbc:sqlite:todo_list.db";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS tasks (\n "  
            + "description TEXT NOT NULL,\n" 
            + "completed BOOLEAN DEFAULT 0 \n" 
            + "); ";

    private static final String INSERT_TASK = "INSERT INTO tasks (description, completed) VALUES (?, ?)";
    private static final String SELECT_ALL_TASKS = "SELECT * FROM tasks";
    private static final String UPDATE_TASKS = "UPDATE tasks SET completed = ? WHERE description = ?";

	private static Connection connection;
	private static SQLiteDataSource dataSource;
	private static JFrame frame;
	private static DefaultListModel<String> todoListModel;

    private static void initDatabase(){
        try{
        dataSource = new SQLiteDataSource();
        dataSource.setUrl(DATABASE_URL);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        try{
            connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate(CREATE_TABLE);
            statement.close();
            connection.close();
        } catch (SQLException e){
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static void addTask(String description) {
        try {
			connection = dataSource.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement(INSERT_TASK);
            preparedStatement.setString(1, description);
            preparedStatement.setBoolean(2, false);
            preparedStatement.executeUpdate();
			preparedStatement.close();
			connection.close();
			JOptionPane.showMessageDialog(frame, "Task added sucessfully");
        } catch (SQLException e){
			JOptionPane.showMessageDialog(frame, "Error addind task"+ e.getMessage());
		}
    }

    private static void removeTask (String task){
        try  {
			connection = dataSource.getConnection();
			PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_TASKS);
            preparedStatement.setBoolean(1, true);
            preparedStatement.setString(2, task);
            preparedStatement.executeUpdate();
			preparedStatement.close();
			connection.close();
			JOptionPane.showMessageDialog(frame, "Task \"" + task + "\" was removed");
        } catch (SQLException e){
			JOptionPane.showMessageDialog(frame,"Error adding expense: " + e.getMessage());
		}
    }

    private static void loadTasks(DefaultListModel<String> listModel) {
        try{ 
            connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SELECT_ALL_TASKS);
            while (resultSet.next()) {
                String task = resultSet.getString("description");
                todoListModel.addElement(task);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e){
			JOptionPane.showMessageDialog(frame, "Error addind task"+ e.getMessage());
		}
    }

	public static void initialize(){
		frame = new JFrame("Expense Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300,400);

        JList <String> todoList = new JList<>();
        todoListModel = new DefaultListModel<>();
        todoList.setModel(todoListModel);
        
        JButton buttonAdd = new JButton("Add");
        buttonAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String task = JOptionPane.showInputDialog(frame, "Create new task:");
                if (task != null && !task.isEmpty()){
                    addTask(task);
                    todoListModel.addElement(task);
                }
            }
        });

        JButton buttonMark = new JButton("Remove");
        buttonMark.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = todoList.getSelectedIndex();
                if(selectedIndex != -1){
                    String selectedTask = todoList.getSelectedValue();
                    removeTask(selectedTask);
                    todoListModel.remove(selectedIndex);
                }
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(buttonAdd);
        buttonPanel.add(buttonMark);

        
        Container contentPane = frame.getContentPane();
        contentPane.add(new JScrollPane(todoList), BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        initDatabase();
        loadTasks(todoListModel);
    }
	

}
