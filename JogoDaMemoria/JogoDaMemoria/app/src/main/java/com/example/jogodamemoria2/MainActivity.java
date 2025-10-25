package com.example.jogodamemoria2;

import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private TextView tvScore, tvMoves, tvTime;
    private Button btnNewGame, btnPause;

    private List<MemoryCard> cards;
    private List<ImageView> cardViews;
    private MemoryCard firstCard, secondCard;
    private int firstCardIndex = -1, secondCardIndex = -1;
    private boolean isProcessing = false;

    private int score = 0;
    private int moves = 0;
    private int matchedPairs = 0;
    private int totalPairs = 0;
    private int comboMultiplier = 1;
    private long startTime;
    private final Handler timeHandler = new Handler();
    private boolean isGameRunning = false;
    private boolean isPaused = false;
    private long timeWhenPaused = 0;
    private boolean isMemorizationPhase = false;
    private int cols;

    private final int[] gameImages = {
            R.drawable.image1, R.drawable.image2, R.drawable.image3,
            R.drawable.image4, R.drawable.image5, R.drawable.image6,
            R.drawable.image7, R.drawable.image8
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupButtons();
        showDifficultyDialog();
    }

    private void initializeViews() {
        gridLayout = findViewById(R.id.gridLayout);
        tvScore = findViewById(R.id.tvScore);
        tvMoves = findViewById(R.id.tvMoves);
        tvTime = findViewById(R.id.tvTime);
        btnNewGame = findViewById(R.id.btnNewGame);
        btnPause = findViewById(R.id.btnPause);
    }

    private void showDifficultyDialog() {
        final boolean gameWasRunning = isGameRunning;
        final boolean gameWasPaused = isPaused;

        if (gameWasRunning && !gameWasPaused) {
            pauseGame();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_difficulty, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        Button btnSimple = dialogView.findViewById(R.id.btnSimple);
        Button btnDifficult = dialogView.findViewById(R.id.btnDifficult);

        btnSimple.setOnClickListener(v -> {
            setupGame(4, 3, 6);
            dialog.dismiss();
        });

        btnDifficult.setOnClickListener(v -> {
            setupGame(4, 4, 8);
            dialog.dismiss();
        });

        dialog.setOnCancelListener(d -> {
            if (gameWasRunning && !gameWasPaused) {
                resumeGame();
            } else if (!gameWasRunning) {
                finish(); // Close the app if the initial dialog is canceled
            }
        });

        if (!gameWasRunning) {
            dialog.setCancelable(true); // Allow canceling with the back button
            dialog.setCanceledOnTouchOutside(false);
        } else {
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
        }

        dialog.show();
    }


    private void setupGame(int rows, int cols, int numPairs) {
        this.cols = cols;
        this.totalPairs = numPairs;
        resetGameStats();
        timeHandler.removeCallbacksAndMessages(null);

        isMemorizationPhase = true;
        btnPause.setEnabled(false);
        btnNewGame.setEnabled(false);

        cards = new ArrayList<>();
        cardViews = new ArrayList<>();

        for (int i = 0; i < numPairs; i++) {
            cards.add(new MemoryCard(gameImages[i], i));
            cards.add(new MemoryCard(gameImages[i], i));
        }
        Collections.shuffle(cards, new Random(System.nanoTime()));

        gridLayout.removeAllViews();
        cardViews.clear();
        gridLayout.setRowCount(rows);
        gridLayout.setColumnCount(cols);

        int marginInPx = (cols == 3) ? dpToPx(8) : dpToPx(4);

        for (int i = 0; i < cards.size(); i++) {
            ImageView cardView = new ImageView(this);
            GridLayout.LayoutParams params;

            if (cols == 3) { // Easy Mode - Rectangular cards
                GridLayout.Spec rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1.3f);
                GridLayout.Spec colSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                params = new GridLayout.LayoutParams(rowSpec, colSpec);
                params.width = 0;
                params.height = 0;
                cardView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else { // Hard Mode - Square cards
                GridLayout.Spec rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                GridLayout.Spec colSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                params = new GridLayout.LayoutParams(rowSpec, colSpec);
                params.width = 0;
                params.height = 0;
                cardView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }

            params.setMargins(marginInPx, marginInPx, marginInPx, marginInPx);
            cardView.setLayoutParams(params);

            cardView.setImageResource(cards.get(i).getImageResource());
            cardView.setBackgroundResource(R.drawable.card_background);
            cardView.setElevation(dpToPx(2));

            final int position = i;
            cardView.setOnClickListener(v -> onCardClick(position));

            cardViews.add(cardView);
            gridLayout.addView(cardView);
        }

        timeHandler.postDelayed(() -> {
            for (ImageView cardView : cardViews) {
                cardView.setImageResource(R.drawable.capa);
            }
            isMemorizationPhase = false;
            btnPause.setEnabled(true);
            btnNewGame.setEnabled(true);
            startGame();
        }, 3000);
    }


    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void onCardClick(int position) {
        if (isMemorizationPhase || isPaused || isProcessing || position >= cards.size() || cards.get(position).isMatched() || cards.get(position).isFlipped()) {
            return;
        }

        MemoryCard clickedCard = cards.get(position);
        ImageView clickedView = cardViews.get(position);

        clickedCard.setFlipped(true);
        clickedView.setImageResource(clickedCard.getImageResource());

        if (firstCard == null) {
            firstCard = clickedCard;
            firstCardIndex = position;
        } else {
            secondCard = clickedCard;
            secondCardIndex = position;
            moves++;
            updateMovesDisplay();

            isProcessing = true;

            if (firstCard.getId() == secondCard.getId()) {
                firstCard.setMatched(true);
                secondCard.setMatched(true);
                matchedPairs++;
                score += 10 * comboMultiplier; // Pontuação com combo
                comboMultiplier++; // Aumenta o combo
                updateScoreDisplay();

                resetSelectedCards();
                checkGameComplete();
            } else {
                comboMultiplier = 1; // Reseta o combo
                score = Math.max(0, score - 2); // Penalidade por erro
                updateScoreDisplay();

                new Handler().postDelayed(() -> {
                    if (firstCardIndex != -1 && secondCardIndex != -1) {
                        if (firstCard != null) firstCard.setFlipped(false);
                        if (secondCard != null) secondCard.setFlipped(false);
                        if (firstCardIndex < cardViews.size())
                            cardViews.get(firstCardIndex).setImageResource(R.drawable.capa);
                        if (secondCardIndex < cardViews.size())
                            cardViews.get(secondCardIndex).setImageResource(R.drawable.capa);
                    }
                    resetSelectedCards();
                }, 1000);
            }
        }
    }

    private void resetSelectedCards() {
        firstCard = null;
        secondCard = null;
        firstCardIndex = -1;
        secondCardIndex = -1;
        isProcessing = false;
    }

    private void checkGameComplete() {
        if (matchedPairs == totalPairs) {
            isGameRunning = false;
            timeHandler.removeCallbacksAndMessages(null);
            showGameOverDialog();
        }
    }

    private void showGameOverDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_game_over, null);
        builder.setView(dialogView);

        TextView tvFinalScore = dialogView.findViewById(R.id.tvFinalScore);
        TextView tvFinalMoves = dialogView.findViewById(R.id.tvFinalMoves);
        Button btnPlayAgain = dialogView.findViewById(R.id.btnPlayAgain);
        Button btnExit = dialogView.findViewById(R.id.btnExit);
        LinearLayout starsLayout = dialogView.findViewById(R.id.starsLayout);

        updateStars(starsLayout);

        tvFinalScore.setText("Pontuação: " + score);
        tvFinalMoves.setText("Movimentos: " + moves);

        final AlertDialog dialog = builder.create();

        btnPlayAgain.setOnClickListener(v -> {
            dialog.dismiss();
            showDifficultyDialog();
        });

        btnExit.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void updateStars(LinearLayout starsLayout) {
        starsLayout.removeAllViews();
        int numStars = calculateStars();

        for (int i = 0; i < 5; i++) {
            ImageView star = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(32),
                    dpToPx(32)
            );
            if (i > 0) {
                params.setMarginStart(dpToPx(8));
            }
            star.setLayoutParams(params);

            if (i < numStars) {
                star.setImageResource(R.drawable.ic_star);
            } else {
                star.setImageResource(R.drawable.ic_star_border);
            }
            starsLayout.addView(star);
        }
    }

    private int calculateStars() {
        int maxScore = 0;
        for (int i = 1; i <= totalPairs; i++) {
            maxScore += 10 * i;
        }

        double performance = (double) score / maxScore;

        if (performance >= 0.9) return 5;
        if (performance >= 0.7) return 4;
        if (performance >= 0.5) return 3;
        if (performance >= 0.3) return 2;
        if (score > 0) return 1;
        return 0;
    }

    private void setupButtons() {
        btnNewGame.setOnClickListener(v -> showDifficultyDialog());

        btnPause.setOnClickListener(v -> {
            if (!isGameRunning) return;
            if (isPaused) {
                resumeGame();
            } else {
                pauseGame();
            }
        });
    }

    private void startGame() {
        startTime = System.currentTimeMillis();
        isGameRunning = true;
        isPaused = false;
        startTimeCounter();
    }

    private void pauseGame() {
        if (isMemorizationPhase) return;
        isPaused = true;
        btnPause.setText("Retomar");
        timeHandler.removeCallbacksAndMessages(null);
        timeWhenPaused = System.currentTimeMillis() - startTime;

        for (int i = 0; i < cards.size(); i++) {
            if (!cards.get(i).isMatched()) {
                cardViews.get(i).setImageResource(R.drawable.capa);
            }
        }
    }

    private void resumeGame() {
        isPaused = false;
        btnPause.setText("Pausar");
        startTime = System.currentTimeMillis() - timeWhenPaused;
        startTimeCounter();

        for (int i = 0; i < cards.size(); i++) {
            MemoryCard card = cards.get(i);
            if (card.isFlipped() && !card.isMatched()) {
                cardViews.get(i).setImageResource(card.getImageResource());
            }
        }
    }

    private void resetGameStats() {
        score = 0;
        moves = 0;
        matchedPairs = 0;
        comboMultiplier = 1; // Reseta o combo
        isGameRunning = false;
        updateScoreDisplay();
        updateMovesDisplay();
        tvTime.setText("Tempo: 00:00");
        resetSelectedCards();
        timeHandler.removeCallbacksAndMessages(null);
    }

    private void updateScoreDisplay() {
        tvScore.setText("Pontuação: " + score);
    }

    private void updateMovesDisplay() {
        tvMoves.setText("Movimentos: " + moves);
    }

    private void startTimeCounter() {
        timeHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isGameRunning && !isPaused) {
                    long elapsed = System.currentTimeMillis() - startTime;
                    int seconds = (int) (elapsed / 1000) % 60;
                    int minutes = (int) (elapsed / 60000);

                    tvTime.setText(String.format(Locale.getDefault(), "Tempo: %02d:%02d", minutes, seconds));
                    timeHandler.postDelayed(this, 1000);
                }
            }
        });
    }

    // --- Added Missing MemoryCard Class ---
    class MemoryCard {
        private int imageResource;
        private int id;
        private boolean isFlipped = false;
        private boolean isMatched = false;

        public MemoryCard(int imageResource, int id) {
            this.imageResource = imageResource;
            this.id = id;
        }

        public int getImageResource() {
            return imageResource;
        }

        public int getId() {
            return id;
        }

        public boolean isFlipped() {
            return isFlipped;
        }

        public void setFlipped(boolean flipped) {
            isFlipped = flipped;
        }

        public boolean isMatched() {
            return isMatched;
        }

        public void setMatched(boolean matched) {
            isMatched = matched;
        }
    }
}
