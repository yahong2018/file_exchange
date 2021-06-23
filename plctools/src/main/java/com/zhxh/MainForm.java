package com.zhxh;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class MainForm extends JFrame {
    private final static int MAX_WIDHT = 800;
    private final static int MAX_HEIGHT = 500;

    private JTextField textFieldIp;
    private JTextField textFieldPort;
    private JComboBox comboBoxAreaByWord;
    private JComboBox comboBoxAreaByBit;
    private JTextField textFieldAddrByWord;

    public MainForm(){
        JFrame.setDefaultLookAndFeelDecorated(true);

        this.setTitle("Omron PLC 测试工具");
        this.setResizable(false);
        this.setUndecorated(true);
        this.getRootPane().setWindowDecorationStyle(JRootPane.INFORMATION_DIALOG);        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new FlowLayout(FlowLayout.LEFT,10,10));    

        this.add(createConnectPanel());       
        this.add(createReadByWordPanel());    
        this.add(createReadByBitPanel());

        this.setBounds(0, 0, MAX_WIDHT,MAX_HEIGHT);
        this.setLocationRelativeTo(null);        
        this.setVisible(true);
    }      

    private JComponent createConnectPanel(){
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("连接设置"));

        panel.setLayout(new FlowLayout(FlowLayout.LEFT,10,10));      

        JLabel  ipLabel = new JLabel("IP：");        
        panel.add(ipLabel);
        this.textFieldIp = new JTextField(); //"255.255.255.255"
        textFieldIp.setPreferredSize(new Dimension(120,25));
        panel.add(textFieldIp);

        JLabel portLabel = new JLabel("端口：");
        panel.add(portLabel);
        textFieldPort = new JTextField(); // 9600
        textFieldPort.setPreferredSize(new Dimension(60,25));
        panel.add(textFieldPort);

        JButton connectButton = new JButton("连接");
        panel.add(connectButton);

        JButton disConnectButton = new JButton("断开");
        disConnectButton.setEnabled(false);
        panel.add(disConnectButton);

        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(100,25));
        panel.add(label);
        JButton shakeButton = new JButton("握手");
        shakeButton.setEnabled(false);
        panel.add(shakeButton);

        return panel;
    }

    private JComponent createReadByWordPanel(){
        JPanel panelReadByWord = new JPanel();        
        panelReadByWord.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));    
        panelReadByWord.setPreferredSize(new Dimension(MAX_WIDHT, 210));     
        panelReadByWord.setBorder(BorderFactory.createTitledBorder("按字读取"));

        panelReadByWord.add(createReadByWordReadPanel());
        panelReadByWord.add(createReadByWordResultPanel());
        panelReadByWord.add(createReadByWordParsedResultPanel());
        panelReadByWord.add(createReadByWordConvertorPanel());

        return panelReadByWord;
    }

    private JComboBox createAreaComboBox(){
        JComboBox combo=new JComboBox();
        combo.setPreferredSize(new Dimension(55,25));
        combo.addItem("CIO");
        combo.addItem("DM");
        combo.addItem("WR");
        combo.addItem("HR");
        combo.addItem("AR");

        return combo;
    }

    private JComponent createReadByWordReadPanel(){
        JPanel panel = new JPanel();        
        panel.setLayout(new FlowLayout(FlowLayout.LEFT,10,10));  

        JLabel labelArea = new JLabel("区域：");
        panel.add(labelArea);

        this.comboBoxAreaByWord = createAreaComboBox();      
        panel.add(this.comboBoxAreaByWord);

        JLabel labelAddr = new JLabel("地址：");
        panel.add(labelAddr);
        this.textFieldAddrByWord = new JTextField();
        textFieldAddrByWord.setPreferredSize(new Dimension(70,25));
        panel.add(textFieldAddrByWord);

        JLabel labelLength = new JLabel("长度：");
        panel.add(labelLength);
        JTextField lengthText = new JTextField();
        lengthText.setPreferredSize(new Dimension(50,25));
        panel.add(lengthText);

        JButton readButton = new JButton("读取");
        readButton.setEnabled(false);
        panel.add(readButton);

        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(180,30));
        panel.add(label);

        return panel;
    }

    private JComponent createReadByWordResultPanel(){
        JPanel panel = new JPanel();        
        panel.setLayout(new FlowLayout(FlowLayout.LEFT,10,10));  

        JLabel labelResult = new JLabel("返回结果：");
        labelResult.setPreferredSize(new Dimension(80,25));
        panel.add(labelResult);
        JTextField rawText = new JTextField();
        rawText.setPreferredSize(new Dimension(650,25));
        panel.add(rawText);
        rawText.setEnabled(false);

        return panel;
    }

    private JComponent createReadByWordParsedResultPanel(){
        JPanel panel = new JPanel();        
        panel.setLayout(new FlowLayout(FlowLayout.LEFT,10,10));  

        JLabel labelResult = new JLabel("解析后结果：");
        labelResult.setPreferredSize(new Dimension(80,25));
        panel.add(labelResult);
        
        JTextField parsedText = new JTextField();
        parsedText.setPreferredSize(new Dimension(650,25));
        panel.add(parsedText);

        return panel;
    }

    private JComponent createReadByWordConvertorPanel(){
        JPanel panel = new JPanel();        
        panel.setLayout(new FlowLayout(FlowLayout.LEFT,10,10));  

        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(80,25));
        panel.add(label);

        JButton buttonBytes = new JButton("字节");
        panel.add(buttonBytes);

        JButton buttonInt = new JButton("整数");
        panel.add(buttonInt);

        JButton buttonFloat = new JButton("浮点");
        panel.add(buttonFloat);

        JButton buttonString = new JButton("字符");
        panel.add(buttonString);

        return panel;
    }

    private JComponent createReadByBitPanel(){
        JPanel panelReadByBit = new JPanel();        
        panelReadByBit.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));    
        panelReadByBit.setPreferredSize(new Dimension(MAX_WIDHT, 190));     
        panelReadByBit.setBorder(BorderFactory.createTitledBorder("按位读取"));

        panelReadByBit.add(createReadByBitReadPanel());
        panelReadByBit.add(createReadByBitResultPanel());
        panelReadByBit.add(createReadByBitParsedResultPanel());

        return panelReadByBit;
    }

    private JComponent createReadByBitReadPanel(){
        JPanel panel = new JPanel();        
        panel.setLayout(new FlowLayout(FlowLayout.LEFT,10,10));  

        JLabel labelArea = new JLabel("区域：");
        panel.add(labelArea);
        this.comboBoxAreaByBit = createAreaComboBox();
        panel.add(this.comboBoxAreaByBit);

        JLabel labelAddr = new JLabel("地址：");
        panel.add(labelAddr);
        JTextField addrText = new JTextField();
        addrText.setPreferredSize(new Dimension(70,25));
        panel.add(addrText);

        JLabel labelIndex = new JLabel("索引：");
        panel.add(labelIndex);
        JTextField indexText = new JTextField();
        indexText.setPreferredSize(new Dimension(50,25));
        panel.add(indexText);

        JButton readButton = new JButton("读取");
        readButton.setEnabled(false);
        panel.add(readButton);

        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(180,30));
        panel.add(label);

        return panel;
    }

    private JComponent createReadByBitResultPanel(){
        JPanel panel = new JPanel();        
        panel.setLayout(new FlowLayout(FlowLayout.LEFT,10,10));  

        JLabel labelResult = new JLabel("返回结果：");
        labelResult.setPreferredSize(new Dimension(80,25));
        panel.add(labelResult);
        JTextField rawText = new JTextField();
        rawText.setPreferredSize(new Dimension(650,25));
        panel.add(rawText);
        rawText.setEnabled(false);

        return panel;
    }

    private JComponent createReadByBitParsedResultPanel(){
        JPanel panel = new JPanel();        
        panel.setLayout(new FlowLayout(FlowLayout.LEFT,10,10));  

        JLabel labelResult = new JLabel("解析后结果：");
        labelResult.setPreferredSize(new Dimension(80,25));
        panel.add(labelResult);
        
        JTextField parsedText = new JTextField();
        parsedText.setPreferredSize(new Dimension(650,25));
        panel.add(parsedText);

        return panel;
    }
}
