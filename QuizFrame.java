import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer; // Explicitly use Swing Timer

public class QuizFrame extends JFrame {
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int totalTime = 15; // seconds
    private Timer timer;

    private JLabel questionLabel, timerLabel;
    private JRadioButton[] options;
    private ButtonGroup optionsGroup;
    private JButton submitButton;
    private JTextArea reviewArea;

    public QuizFrame(String questionFilePath) {
        questions = loadQuestionsFromCSV(questionFilePath);
        if (questions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No questions found!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        setTitle("Quiz Application");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set background color for the main content pane
        getContentPane().setBackground(new Color(245, 245, 255)); // light blueish

        questionLabel = new JLabel();
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        questionLabel.setForeground(new Color(33, 37, 41));
        questionLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        timerLabel = new JLabel("Time: " + totalTime + "s");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        timerLabel.setForeground(new Color(220, 53, 69));
        timerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        timerLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        options = new JRadioButton[4];
        optionsGroup = new ButtonGroup();
        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 0, 10));
        optionsPanel.setBackground(new Color(245, 245, 255));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            options[i].setFont(new Font("Segoe UI", Font.PLAIN, 16));
            options[i].setBackground(new Color(255, 255, 255));
            options[i].setFocusPainted(false);
            optionsGroup.add(options[i]);
            optionsPanel.add(options[i]);
        }

        submitButton = new JButton("Submit");
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        submitButton.setBackground(new Color(40, 167, 69));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        submitButton.addActionListener(e -> checkAnswer());

        reviewArea = new JTextArea();
        reviewArea.setEditable(false);
        reviewArea.setVisible(false);
        reviewArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        reviewArea.setBackground(new Color(255, 255, 240));
        reviewArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(245, 245, 255));
        panel.add(questionLabel, BorderLayout.NORTH);
        panel.add(optionsPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(245, 245, 255));
        bottomPanel.add(submitButton);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        add(panel, BorderLayout.CENTER);
        add(timerLabel, BorderLayout.NORTH);
        add(reviewArea, BorderLayout.SOUTH);

        loadNextQuestion();
        setVisible(true);
    }

    private List<Question> loadQuestionsFromCSV(String path) {
        List<Question> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    list.add(new Question(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void loadNextQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            showResults();
            return;
        }

        Question q = questions.get(currentQuestionIndex);
        questionLabel.setText((currentQuestionIndex + 1) + ". " + q.question);
        options[0].setText(q.option1);
        options[1].setText(q.option2);
        options[2].setText(q.option3);
        options[3].setText(q.option4);
        optionsGroup.clearSelection();

        if (timer != null)
            timer.stop();
        totalTime = 15;
        timerLabel.setText("Time: " + totalTime + "s");
        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                totalTime--;
                timerLabel.setText("Time: " + totalTime + "s");
                if (totalTime <= 0) {
                    timer.stop();
                    checkAnswer(); // auto-submit on timeout
                }
            }
        });
        timer.start();
    }

    private void checkAnswer() {
        timer.stop();
        Question q = questions.get(currentQuestionIndex);
        String selectedAnswer = null;
        for (JRadioButton option : options) {
            if (option.isSelected()) {
                selectedAnswer = option.getText();
                break;
            }
        }
        if (selectedAnswer != null && selectedAnswer.equals(q.correctAnswer)) {
            score += 10;
        }
        currentQuestionIndex++;
        loadNextQuestion();
    }

    private void showResults() {
        StringBuilder result = new StringBuilder();
        result.append("Your Score: ").append(score).append("\n\nAnswer Review:\n");
        for (Question q : questions) {
            result.append(q.question).append("\nCorrect: ").append(q.correctAnswer).append("\n\n");
        }

        questionLabel.setVisible(false);
        for (JRadioButton rb : options)
            rb.setVisible(false);
        submitButton.setVisible(false);
        timerLabel.setVisible(false);
        reviewArea.setVisible(true);
        reviewArea.setText(result.toString());
        pack();
    }
}
