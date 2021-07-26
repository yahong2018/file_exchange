package com.zhxh;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;

import com.zhxh.imms.omron.backgroud.OmronPlc;
import com.zhxh.imms.utils.ByteUtil;

public class MainForm extends JFrame {
    private final static int MAX_WIDHT = 800;
    private final static int MAX_HEIGHT = 550;

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
    private JTextField textFieldLength;

    private JButton connectButton;
    private JButton disConnectButton;
    private JButton shakeButton;
    private JButton readByWordButton;
    private JButton buttonBytes;
    private JButton buttonInt;
    private JButton buttonFloat;
    private JButton buttonString;
    private JButton readByBitButton;
    private JButton verifyButton;

    private byte[] lastWordBuffer = new byte[] {};
    private byte[] lastBitBuffer = new byte[] {};
    private byte[] dataBuffer = new byte[] {};

    private OmronPlc omronPlc = new OmronPlc();

    public MainForm() {
        JFrame.setDefaultLookAndFeelDecorated(false);

        createUI();
        createEventListeners();

        this.setVisible(true);
    }

    private void connect() {
        String ip = this.textFieldIp.getText();
        String strPort = this.textFieldPort.getText();
        if (ip.isEmpty() || strPort.isEmpty()) {
            JOptionPane.showMessageDialog(null, "IP地址和端口都必须输入！", "系统提示", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            int port = Integer.parseInt(strPort);
            this.omronPlc.setIp(ip);
            this.omronPlc.setPort(port);

            this.omronPlc.connect();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "系统连接出现错误:" + e.getMessage(), "系统提示", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        this.onConnected();
    }

    private void disConnect() {
        this.omronPlc.disConnect();

        this.onDisConnected();
    }

    private void shakeHand() {
        System.out.println("shakeHand");

        this.onShakeHand();
    }

    private void readByWord() {
        String area = this.comboBoxAreaByWord.getSelectedItem().toString();
        String addr = this.textFieldAddrByWord.getText();
        String strLength = this.textFieldLength.getText();
        if (area.isEmpty() || addr.isEmpty() || strLength.isEmpty()) {
            JOptionPane.showMessageDialog(null, "区域、地址和长度都必须输入！", "系统提示", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            int length = Integer.parseInt(strLength);
            byte[] buffer = new byte[1024];
            int readLength = this.omronPlc.rawRead(area, addr, length, buffer, 0);
            this.lastWordBuffer = new byte[readLength];
            System.arraycopy(buffer, 0, this.lastWordBuffer, 0, readLength);
            int dataLength = length * 2;
            this.dataBuffer = new byte[dataLength];
            this.textFieldRawByWord.setText(ByteUtil.bytesToHex(this.lastWordBuffer));
            System.arraycopy(this.lastWordBuffer, readLength - dataLength, this.dataBuffer,0, dataLength);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "按字读取出现错误:" + e.getMessage(), "系统提示", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void reslultToBytes() {
        String hexString = ByteUtil.bytesToHex(this.dataBuffer);
        this.textFieldParsedByWord.setText(hexString);
    }

    private void resultToInt() {
        Integer intResult =  ByteUtil.bytes2Int(this.dataBuffer);
        this.textFieldParsedByWord.setText(intResult.toString());
    }

    private void resultToFloat() {
        Float floatResult = ByteUtil.bytes2Float(this.dataBuffer);
        this.textFieldParsedByWord.setText(floatResult.toString());
    }

    private void resultToString() {
        String strResult = new String(this.dataBuffer);
        this.textFieldParsedByWord.setText(strResult);
    }

    private void readByBit() {
        String area = this.comboBoxAreaByBit.getSelectedItem().toString();
        String addr = this.textFieldAddrByBit.getText();
        String strIndex = this.textFieldIndex.getText();
        if (area.isEmpty() || addr.isEmpty() || strIndex.isEmpty()) {
            JOptionPane.showMessageDialog(null, "区域、地址和索引都必须输入！", "系统提示", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            int index = Integer.parseInt(strIndex);
            byte[] buffer = new byte[1024];
            int readLength = this.omronPlc.rawRead(area, addr, index, buffer, 1);
            this.lastBitBuffer = new byte[readLength];
            System.arraycopy(buffer, 0, this.lastBitBuffer, 0, readLength);

            this.textFieldRawtByBit.setText(ByteUtil.bytesToHex(this.lastBitBuffer));        
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "按位读取出现错误:" + e.getMessage(), "系统提示", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void verify() {
        try {
            this.dataBuffer = this.omronPlc.verifyResultBuffer(this.lastWordBuffer,
                    Integer.parseInt(this.textFieldLength.getText())*2);

            this.textFieldParsedByWord.setText(ByteUtil.bytesToHex(this.dataBuffer));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "校验出现错误:" + e.getMessage(), "系统提示", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void onConnected() {
        this.disConnectButton.setEnabled(true);
        this.shakeButton.setEnabled(true);
        this.connectButton.setEnabled(false);

        this.onShakeHand();
    }

    private void onShakeHand() {
        this.readByWordButton.setEnabled(true);
        this.readByBitButton.setEnabled(true);
        this.buttonBytes.setEnabled(true);
        this.buttonInt.setEnabled(true);
        this.buttonFloat.setEnabled(true);
        this.buttonString.setEnabled(true);
        this.verifyButton.setEnabled(true);

        // this.shakeButton.setEnabled(false);
    }

    private void onDisConnected() {
        this.readByWordButton.setEnabled(false);
        this.readByBitButton.setEnabled(false);
        this.buttonBytes.setEnabled(false);
        this.buttonInt.setEnabled(false);
        this.buttonFloat.setEnabled(false);
        this.buttonString.setEnabled(false);

        // this.shakeButton.setEnabled(false);
        this.connectButton.setEnabled(true);
        this.disConnectButton.setEnabled(false);
        this.verifyButton.setEnabled(false);
    }

    private void createEventListeners() {
        this.connectButton.addActionListener(e -> connect());
        this.disConnectButton.addActionListener(e -> disConnect());
        this.shakeButton.addActionListener(e -> shakeHand());
        this.readByWordButton.addActionListener(e -> readByWord());
        this.buttonBytes.addActionListener(e -> reslultToBytes());
        this.buttonInt.addActionListener(e -> resultToInt());
        this.buttonFloat.addActionListener(e -> resultToFloat());
        this.buttonString.addActionListener(e -> resultToString());
        this.readByBitButton.addActionListener(e -> readByBit());
        this.verifyButton.addActionListener(e -> verify());
    }

    private void createUI() {
        this.setTitle("Omron PLC 测试工具");
        this.setResizable(false);
        // this.setUndecorated(true);
        // this.getRootPane().setWindowDecorationStyle(JRootPane.INFORMATION_DIALOG);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        this.add(createConnectPanel());
        this.add(createReadByWordPanel());
        this.add(createReadByBitPanel());

        this.setBounds(0, 0, MAX_WIDHT, MAX_HEIGHT);
        this.setLocationRelativeTo(null);
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
        combo.setPreferredSize(new Dimension(85, 25));
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
        this.textFieldLength = new JTextField();
        textFieldLength.setPreferredSize(new Dimension(50, 25));
        panel.add(textFieldLength);

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

        this.verifyButton = new JButton("校验");
        verifyButton.setEnabled(false);
        panel.add(verifyButton);

        this.buttonBytes = new JButton("字节");
        buttonBytes.setEnabled(false);
        panel.add(buttonBytes);

        this.buttonInt = new JButton("整数");
        buttonInt.setEnabled(false);
        panel.add(buttonInt);

        this.buttonFloat = new JButton("浮点");
        buttonFloat.setEnabled(false);
        panel.add(buttonFloat);

        this.buttonString = new JButton("字符");
        buttonString.setEnabled(false);
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
