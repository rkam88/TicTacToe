package net.rusnet.sb.tictactoe;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String ARG_SCORE_X = "ARG_SCORE_X";
    public static final String ARG_SCORE_O = "ARG_SCORE_O";
    public static final String ARG_STATUS = "ARG_STATUS";
    public static final String ARG_X_TURN = "ARG_X_TURN";
    public static final String ARG_FIELD = "ARG_FIELD";
    public static final String ARG_WINNER_RESOURCE = "ARG_WINNER_RESOURCE";

    private boolean isXTurn;
    private int mBoardSize = 3;
    private ImageView[][] mCellArray = new ImageView[mBoardSize][mBoardSize];
    private TicTacToeField mField;

    private int mXWinCount = 0, mOWinCount = 0;
    private TextView mXWinCountTextView, mOWinCountTextView;

    private GameStatus mGameStatus = GameStatus.STARTING;

    private LinearLayout mEndGameBox;
    private TextView mWinnerTextView;
    private Button mRestartButton, mCloseButton;

    private int mCurrentWinnerResource = R.string.empty;

    @Override
    public void onClick(View v) {
        if (mGameStatus == GameStatus.ENDED) return;

        int figureToPlace;
        figureToPlace = isXTurn ? R.drawable.cross : R.drawable.circle;
        int xCoordinate = Integer.parseInt(v.getTag(R.id.x_coordinate).toString());
        int yCoordinate = Integer.parseInt(v.getTag(R.id.y_coordinate).toString());

        if (mField.isEmptyCell(xCoordinate, yCoordinate)) {
            ((ImageView) v).setImageResource(figureToPlace);
            mField.setFigure(xCoordinate,
                    yCoordinate,
                    isXTurn ? TicTacToeField.Figure.CROSS : TicTacToeField.Figure.CIRCLE);

            ObjectAnimator figureAppearAnimation = ObjectAnimator.ofFloat(
                    v,
                    View.ALPHA,
                    0.5f,
                    1.0f
            );
            figureAppearAnimation.setDuration(250);
            figureAppearAnimation.start();

            isXTurn = !isXTurn;

            checkAndResolveGameEnd();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mXWinCount = savedInstanceState.getInt(ARG_SCORE_X);
            mOWinCount = savedInstanceState.getInt(ARG_SCORE_O);
            mGameStatus = (GameStatus) savedInstanceState.getSerializable(ARG_STATUS);
            isXTurn = savedInstanceState.getBoolean(ARG_X_TURN);
            mField = savedInstanceState.getParcelable(ARG_FIELD);
            mCurrentWinnerResource = savedInstanceState.getInt(ARG_WINNER_RESOURCE);
        }

        initGameBoard();
        initScoreCounter();
        if (mGameStatus == GameStatus.STARTING) setupBoardForGameStart();
        else recreateGameBoard();
        initEndGameBox();
        if (mGameStatus == GameStatus.ENDED) {
            mWinnerTextView.setText(mCurrentWinnerResource);
            showEndGameBox();
        }

    }

    private void checkAndResolveGameEnd() {
        TicTacToeField.Figure winner = mField.getWinner();

        if (winner != TicTacToeField.Figure.NONE) {
            mGameStatus = GameStatus.ENDED;
            if (winner == TicTacToeField.Figure.CROSS) {
                mXWinCount++;
                mXWinCountTextView.setText(String.valueOf(mXWinCount));
                mCurrentWinnerResource = R.string.Crosses;
                mWinnerTextView.setText(mCurrentWinnerResource);
            } else if (winner == TicTacToeField.Figure.CIRCLE) {
                mOWinCount++;
                mOWinCountTextView.setText(String.valueOf(mOWinCount));
                mCurrentWinnerResource = R.string.Circles;
                mWinnerTextView.setText(mCurrentWinnerResource);
            }
            showEndGameBox();
        } else if (mField.isFull()) {
            mGameStatus = GameStatus.ENDED;
            mCurrentWinnerResource = R.string.Draw;
            mWinnerTextView.setText(mCurrentWinnerResource);
            mEndGameBox.setVisibility(View.VISIBLE);
        }
    }

    private void initGameBoard() {
        FrameLayout frameLayout = findViewById(R.id.frame_layout);
        FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT
        );

        TableLayout table = new TableLayout(this);
        table.setLayoutParams(frameParams);
        TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT,
                TableLayout.LayoutParams.WRAP_CONTENT);
        frameLayout.addView(table);

        int cellSize = calculateCellSize(mBoardSize);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(cellSize, cellSize);

        for (int i = 0; i < mBoardSize; i++) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(tableParams);
            table.addView(row);
            for (int j = 0; j < mBoardSize; j++) {
                ImageView image = new ImageView(this);
                image.setLayoutParams(rowParams);
                image.setBackgroundResource(R.drawable.cell_border);
                image.setTag(R.id.x_coordinate, i);
                image.setTag(R.id.y_coordinate, j);
                row.addView(image);
                mCellArray[i][j] = image;
            }
        }
        enableBoardOnClickListeners();
    }

    private int calculateCellSize(int numberOfCells) {

        Display display = getWindowManager().getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        int totalLength;
        if (width <= height) {
            totalLength = width;
        } else {
            totalLength = height - gerActionBarHeight() - getStatusBarHeight();
        }
        return totalLength / numberOfCells;
    }

    private int getStatusBarHeight() {
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    private int gerActionBarHeight() {
        int actionBarHeight = 0;
        final TypedArray styledAttributes = this.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize}
        );
        actionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return actionBarHeight;
    }

    private void enableBoardOnClickListeners() {
        for (int i = 0; i < mBoardSize; i++) {
            for (int j = 0; j < mBoardSize; j++) {
                mCellArray[i][j].setOnClickListener(this);
            }
        }
    }

    private void initScoreCounter() {
        mXWinCountTextView = findViewById(R.id.x_win_count_text_view);
        mOWinCountTextView = findViewById(R.id.o_win_count_text_view);
        mXWinCountTextView.setText(String.valueOf(mXWinCount));
        mOWinCountTextView.setText(String.valueOf(mOWinCount));
    }

    private void setupBoardForGameStart() {
        mField = new TicTacToeField(mBoardSize);
        isXTurn = true;
        mGameStatus = GameStatus.IN_PROGRESS;
        for (int i = 0; i < mBoardSize; i++) {
            for (int j = 0; j < mBoardSize; j++) {
                mCellArray[i][j].setImageDrawable(null);
            }
        }
    }

    private void recreateGameBoard() {
        for (int i = 0; i < mBoardSize; i++) {
            for (int j = 0; j < mBoardSize; j++) {
                TicTacToeField.Figure figure = mField.getFigure(i, j);
                if (figure != TicTacToeField.Figure.NONE) {
                    int figureToPlace = (figure == TicTacToeField.Figure.CROSS) ? R.drawable.cross : R.drawable.circle;
                    mCellArray[i][j].setImageResource(figureToPlace);
                }
            }
        }
    }

    private void initEndGameBox() {
        mEndGameBox = findViewById(R.id.end_game_box);
        mWinnerTextView = findViewById(R.id.winner_text_view);
        mRestartButton = findViewById(R.id.restart_button);
        mCloseButton = findViewById(R.id.close_button);

        mEndGameBox.bringToFront();

        mRestartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collection<Animator> disappearAnimations = new ArrayList<>();
                Collection<Animator> appearAnimations = new ArrayList<>();

                for (int i = 0; i < mBoardSize; i++) {
                    for (int j = 0; j < mBoardSize; j++) {
                        ObjectAnimator figureDisappearAnimation = ObjectAnimator.ofFloat(
                                mCellArray[i][j],
                                View.ALPHA,
                                1.0f,
                                0.0f
                        );
                        disappearAnimations.add(figureDisappearAnimation);

                        ObjectAnimator figureAppearAnimation = ObjectAnimator.ofFloat(
                                mCellArray[i][j],
                                View.ALPHA,
                                0.0f,
                                1.0f
                        );
                        appearAnimations.add(figureAppearAnimation);
                    }
                }
                createAndStartAnimationSet(disappearAnimations, appearAnimations);
                hideEndGameBox();
            }
        });

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showEndGameBox() {
        mEndGameBox.setVisibility(View.VISIBLE);
        Animation animationShowEndGameBox = AnimationUtils.loadAnimation(this, R.anim.end_game_box_appear);
        mEndGameBox.startAnimation(animationShowEndGameBox);
    }

    private void hideEndGameBox() {
        Animation animationHideEndGameBox = AnimationUtils.loadAnimation(this, R.anim.end_game_box_disappear);
        mEndGameBox.startAnimation(animationHideEndGameBox);
        mEndGameBox.setVisibility(View.INVISIBLE);
    }

    private void createAndStartAnimationSet(@NonNull Collection<Animator> currentAnimations, @Nullable final Collection<Animator> nextAnimations) {
        AnimatorSet set = new AnimatorSet();

        set.setDuration(500);
        set.playTogether(currentAnimations);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (nextAnimations != null) {
                    setupBoardForGameStart();
                    createAndStartAnimationSet(nextAnimations, null);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.start();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(ARG_SCORE_X, mXWinCount);
        outState.putInt(ARG_SCORE_O, mOWinCount);
        outState.putSerializable(ARG_STATUS, mGameStatus);
        outState.putBoolean(ARG_X_TURN, isXTurn);
        outState.putParcelable(ARG_FIELD, mField);
        outState.putInt(ARG_WINNER_RESOURCE, mCurrentWinnerResource);
    }

    private enum GameStatus {
        STARTING,
        IN_PROGRESS,
        ENDED
    }

}
