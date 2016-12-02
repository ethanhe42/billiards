package zhuoqiu;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;



/**
 * 加载菜单
 */
public class Menu extends JFrame implements ActionListener {
	public static final int WINDOW_X = 1100;
	public static final int WINDOW_Y = 725;


    // widgets
    JLabel status; // to set status messages
    Game  game;  // to reference the playing table
    
    /**
     * default constructor.
     * builds the window
     */
    public Menu() {
        // always call super
        super();

        // makes window look more natural
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        // set jframe basics
        setTitle("桌球游戏");


        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setSize( WINDOW_X, WINDOW_Y );

        setResizable( true );
        setLocationByPlatform(true);

        // labels
        status = new JLabel("何宜晖，谢茹吉", JLabel.CENTER);
        add(status, BorderLayout.SOUTH);

        // table
        game = new Game(this);
        add(game, BorderLayout.CENTER);
        
        // menus
        buildMenus();

        // let's see it!
        setVisible(true);
        requestFocus();
    }


    /****************************************
    * private method to build the menus
    ***************************************/
    private void buildMenus() {
        JMenuBar menubar = new JMenuBar();

        // menus
        JMenu file = new JMenu("游戏"),
              opts = new JMenu("选项"),
              help = new JMenu("帮助");

        menubar.add(file);
        menubar.add(opts);
        menubar.add(help);

        // file menu
        addMenuItems(file, "新游戏, -, 退出游戏");

        // options menu
        addMenuItems(opts, "*Friction, *Aim Help, -");

        // help menu
        addMenuItems(help, "关于");

        // lets see it!
        setJMenuBar(menubar);
    }

    /***************************************************************
     * method to add menu items to a menu. it got annoying coding
     * this manually for each item so i wrote this function to do
     * it all in one go. just pass it the menu and a string of the
     * items seperated by commas. use a dash "-" for a seperator,
     * pound "#" for check box, and star "*" for checked check box.
     *
     * @param menu menu to add items too
     * @param items the items
     ***************************************************************/
    private void addMenuItems(JMenu menu, String items) {
        JMenuItem menuitem;

        // loop through items adding them to menu
        for (String s : items.split(", ")) {
            if (s.equals("-"))
                menu.addSeparator();
            else {
                if (s.substring(0, 1).equals("*")) // menu types
                    menuitem = new JCheckBoxMenuItem(s.substring(1), true);
                else if (s.substring(0, 1).equals("#"))
                    menuitem = new JCheckBoxMenuItem(s.substring(1));
                else
                    menuitem = new JMenuItem(s);

                menuitem.addActionListener(this);
                menu.add(menuitem);
            }
        }
    }


    /****************************************
     * sets the label to a status message
     *
     * @param msg message
     ****************************************/
    public void setStatus(String msg) {
        status.setText(msg);
    }



	/***************************************
	*
	*
	*
	***************************************/
	public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();

        if (s.equals("退出游戏"))
            System.exit(0);

        else if (s.equals("新游戏")) {
        	System.out.println("新游戏.");
        	game.newGame();
        }

        else if (s.equals("Friction"))
            game.yinqing.toggleFriction();

        else if (s.equals("Aim Help"))
            game.yinqing.toggleAimHelp();

        else if (s.equals("关于")) // about dialog
            JOptionPane.showMessageDialog(this,
                    "课程：Java技术与应用\n" +
                    "何宜晖 计算机46 \n" +
                    "谢茹吉 自动化46 \n",
                    "关于作者", JOptionPane.DEFAULT_OPTION);
        else
            System.out.println(s);
    }

}
