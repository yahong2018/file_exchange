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
    private JComboBox<String> comboBoxAreaByWord;
    private JComboBox<String> comboBoxAreaByBit;
    private JTextField textFieldAddrByWord;
    private JTextField textFieldAddrByBit;
    private JTextField textFieldIndex;
    private JTextField textFieldParsedByWord;
    private JTextField textFieldRawByWord;
    private JTextField textFieldRawtByBit;
    private JTextField textFieldParsedByBit;

    private JButton connectButton;
    private JButton disConnectButton;
    private JButton shakeButton;
    private JButton readByWordButton;
    private JButton buttonBytes;
    private JButton buttonInt;
    private JButton buttonFloat;
    private JButton buttonString;
    private JButton readByBitButton;

    public MainForm() {
        JFrame.setDefaultLookAndFeelDecorated(true);

        this.setTitle("Omron PLC 测试工具");
        this.setResizable(false);
        this.setUndecorated(true);
        this.getRootPane().setWindowDecorationStyle(JRootPane.INFORMATION_DIALOG);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        this.add(createConnectPanel());
        this.add(createReadByWordPanel());
        this.add(createReadByBitPanel());

        this.setBounds(0, 0, MAX_WIDHT, MAX_HEIGHT);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private JComponent createConnectPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("连接设置"));

        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JLabel ipLabel = new JLabel("IP：");
        panel.add(ipLabel);
        this.textFieldIp = new JTextField(); // "255.255.255.255"
        textFieldIp.setPreferredSize(new Dimension(120, 25));
        panel.add(textFieldIp);

        JLabel portLabel = new JLabel("端口：");
        panel.add(portLabel);
        textFieldPort = new JTextField(); // 9600
        textFieldPort.setPreferredSize(new Dimension(60, 25));
        panel.add(textFieldPort);

        this.connectButton = new JButton("连接");
        panel.add(connectButton);

        this.disConnectButton = new JButton("断开");
        disConnectButton.setEnabled(false);
        panel.add(disConnectButton);

        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(100, 25));
        panel.add(label);
        this.shakeButton = new JButton("握手");
        shakeButton.setEnabled(false);
        panel.add(shakeButton);

        return panel;
    }

    private JComponent createReadByWordPanel() {
        JPanel panelReadByWord = new JPanel();
        panelReadByWord.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelReadByWord.setPreferredSize(new Dimension(MAX_WIDHT, 210));
        panelReadByWord.setBorder(BorderFactory.createTitledBorder("按字读取"));

        panelReadByWord.add(createReadByWordReadPanel());
        panelReadByWord.add(createReadByWordResultPanel());
        panelReadByWord.add(createReadByWordParsedResultPanel());
        panelReadByWord.add(createReadByWordConvertorPanel());

        return panelReadByWord;
    }

    private JComboBox<String> createAreaComboBox() {
        JComboBox<String> combo = new JComboBox<String>();
        combo.setPreferredSize(new Dimension(55, 25));
        combo.addItem("CIO");
        combo.addItem("DM");
        combo.addItem("WR");
        combo.addItem("HR");
        combo.addItem("AR");

        return combo;
    }

    private JComponent createReadByWordReadPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JLabel labelArea = new JLabel("区域：");
        panel.add(labelArea);

        this.comboBoxAreaByWord = createAreaComboBox();
        panel.add(this.comboBoxAreaByWord);

        JLabel labelAddr = new JLabel("地址：");
        panel.add(labelAddr);
        this.textFieldAddrByWord = new JTextField();
        textFieldAddrByWord.setPreferredSize(new Dimension(70, 25));
        panel.add(textFieldAddrByWord);

        JLabel labelLength = new JLabel("长度：");
        panel.add(labelLength);
        JTextField lengthText = new JTextField();
        lengthText.setPreferredSize(new Dimension(50, 25));
        panel.add(lengthText);

        this.readByWordButton = new JButton("读取");
        readByWordButton.setEnabled(false);
        panel.add(readByWordButton);

        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(180, 30));
        panel.add(label);

        return panel;
    }

    private JComponent createReadByWordResultPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JLabel labelResult = new JLabel("返回结果：");
        labelResult.setPreferredSize(new Dimension(80, 25));
        panel.add(labelResult);
        this.textFieldRawByWord = new JTextField();
        textFieldRawByWord.setPreferredSize(new Dimension(650, 25));
        panel.add(textFieldRawByWord);
        textFieldRawByWord.setEnabled(false);

        return panel;
    }

    private JComponent createReadByWordParsedResultPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JLabel labelResult = new JLabel("解析后结果：");
        labelResult.setPreferredSize(new Dimension(80, 25));
        panel.add(labelResult);
        this.textFieldParsedByWord = new JTextField();
        textFieldParsedByWord.setPreferredSize(new Dimension(650, 25));
        panel.add(textFieldParsedByWord);

        return panel;
    }

    private JComponent createReadByWordConvertorPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(80, 25));
        panel.add(label);

        this.buttonBytes = new JButton("字节");
        panel.add(buttonBytes);

        this.buttonInt = new JButton("整数");
        panel.add(buttonInt);

        this.buttonFloat = new JButton("浮点");
        panel.add(buttonFloat);

        this.buttonString = new JButton("字符");
        panel.add(buttonString);

        return panel;
    }

    private JComponent createReadByBitPanel() {
        JPanel panelReadByBit = new JPanel();
        panelReadByBit.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelReadByBit.setPreferredSize(new Dimension(MAX_WIDHT, 190));
        panelReadByBit.setBorder(BorderFactory.createTitledBorder("按位读取"));

        panelReadByBit.add(createReadByBitReadPanel());
        panelReadByBit.add(createReadByBitResultPanel());
        panelReadByBit.add(createReadByBitParsedResultPanel());

        return panelReadByBit;
    }

    private JComponent createReadByBitReadPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JLabel labelArea = new JLabel("区域：");
        panel.add(labelArea);
        this.comboBoxAreaByBit = createAreaComboBox();
        panel.add(this.comboBoxAreaByBit);

        JLabel labelAddr = new JLabel("地址：");
        panel.add(labelAddr);
        this.textFieldAddrByBit = new JTextField();
        textFieldAddrByBit.setPreferredSize(new Dimension(70, 25));
        panel.add(textFieldAddrByBit);

        JLabel labelIndex = new JLabel("索引：");
        panel.add(labelIndex);
        this.textFieldIndex = new JTextField();
        textFieldIndex.setPreferredSize(new Dimension(50, 25));
        panel.add(textFieldIndex);

        this.readByBitButton = new JButton("读取");
        readByBitButton.setEnabled(false);
        panel.add(readByBitButton);

        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(180, 30));
        panel.add(label);

        return panel;
    }

    private JComponent createReadByBitResultPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JLabel labelResult = new JLabel("返回结果：");
        labelResult.setPreferredSize(new Dimension(80, 25));
        panel.add(labelResult);
        this.textFieldRawtByBit = new JTextField();
        textFieldRawtByBit.setPreferredSize(new Dimension(650, 25));
        panel.add(textFieldRawtByBit);
        textFieldRawtByBit.setEnabled(false);

        return panel;
    }

    private JComponent createReadByBitParsedResultPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JLabel labelResult = new JLabel("解析后结果：");
        labelResult.setPreferredSize(new Dimension(80, 25));
        panel.add(labelResult);
        this.textFieldParsedByBit = new JTextField();
        textFieldParsedByBit.setPreferredSize(new Dimension(650, 25));
        panel.add(textFieldParsedByBit);

        return panel;
    }
}
