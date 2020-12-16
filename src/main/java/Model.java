
import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    protected int score;
    protected int maxTile;

    private Stack<Tile[][]> previousStates = new Stack<>();
    private Stack<Integer>  previousScores = new Stack<>();
    private boolean isSaveNeeded = true;


    public Model(){
        this.gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        resetGameTiles();
    }
    private void saveState(Tile[][] tiles){
        Tile [][] tilesForSave = new Tile[tiles.length][tiles[0].length];
        for(int i = 0; i<tilesForSave.length; i++){
            for(int j = 0; j< tilesForSave[i].length; j++){
                tilesForSave[i][j] = new Tile(tiles[i][j].value);
            }
        }
        previousStates.push(tilesForSave);
        previousScores.push(score);
        isSaveNeeded = false;
    }
    public void rollback(){
        if(!previousStates.isEmpty() && !previousScores.isEmpty()) {
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }
    private boolean compressTiles(Tile[] tiles){
        boolean isChanged = false;
        Tile[] tiles1 = new Tile[tiles.length];
        for(int i=0; i<tiles.length; i++){
            tiles1[i] = new Tile(tiles[i].value);
        }
        for(int i = tiles.length-1 ; i > 0 ; i--){
            for(int j = 0 ; j < i ; j++){
                if( tiles[j].value== 0 ) {
                    int tmp = tiles[j].value;
                    tiles[j].value = tiles[j+1].value;
                    tiles[j+1].value = tmp;
                }
            }
        }
        for(int i=0; i<tiles.length; i++){
            if(tiles[i].value!=tiles1[i].value){
                isChanged = true;
            }
        }
        return isChanged;
    }

    private boolean mergeTiles(Tile[] tiles){
        boolean isChanged = false;
        Tile[] tiles1 = new Tile[tiles.length];
        for(int i=0; i<tiles.length; i++){
            tiles1[i] = new Tile(tiles[i].value);
        }
        for (int i = 1; i < tiles.length ; i++) {
            if (tiles[i-1].value==tiles[i].value){
                tiles[i-1].value*= 2;
                tiles[i].value = 0;
                if (tiles[i-1].value > maxTile){
                    maxTile = tiles[i-1].value;}

                score+=tiles[i-1].value;
            }
        }
        compressTiles(tiles);
        for(int i=0; i<tiles.length; i++){
            if(tiles[i].value!=tiles1[i].value){
                isChanged = true;
            }
        }
        return isChanged;
    }
    public void left(){
        if(isSaveNeeded){saveState(gameTiles);}
        boolean isChanged = false;
        for(int i = 0; i< gameTiles.length; i++){
            boolean a = compressTiles(gameTiles[i]);
            boolean b = mergeTiles(gameTiles[i]);
            if(a || b){
                isChanged = true;
            }
        }
        if(isChanged){
            addTile();
        }
        isSaveNeeded = true;
    }
    public void right(){
        saveState(gameTiles);
        turnLeft();
        turnLeft();
        left();
        turnRight();
        turnRight();
    }
    public void up(){
        saveState(gameTiles);
        turnLeft();
        left();
        turnRight();
    }
    public void down(){
        saveState(gameTiles);
        turnRight();
        left();
        turnLeft();
    }
    public void turnLeft(){
        Tile[][] tiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for(int i=0; i<gameTiles.length;i++){
            for(int j = 0; j<gameTiles.length; j++){
                tiles[FIELD_WIDTH - 1 - j][i] = gameTiles[i][j];
            }
        }
        gameTiles = tiles;
    }
    public void turnRight(){
        Tile[][] tiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for(int i=0; i<gameTiles.length;i++){
            for(int j = 0; j<gameTiles[i].length; j++){
                tiles[i][FIELD_WIDTH - 1 - j] = gameTiles[j][i];
            }
        }
        gameTiles = tiles;
    }

    public void addTile(){
        List<Tile> tiles = getEmptyTiles();
        if(!tiles.isEmpty()) {
            Tile tile = tiles.get((int) (tiles.size() * Math.random()));
            tile.value = (Math.random() < 0.9 ? 2 : 4);
        }
    }
    private List<Tile> getEmptyTiles(){
        List<Tile> tiles = new ArrayList<>();
        for(int i=0; i< gameTiles.length; i++){
            for(int j=0; j<gameTiles[i].length; j++){
                if(gameTiles[i][j].value == 0) {
                    tiles.add(gameTiles[i][j]);
                }
            }
        }
        return tiles;
    }
    public void resetGameTiles(){
        for(int i=0; i< gameTiles.length; i++){
            for(int j=0; j<gameTiles[i].length; j++){
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
        score = 0;
        maxTile = 0;
    }
    public boolean canMove() {
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[0].length; j++) {
                if (gameTiles[i][j].value == 0)
                    return true;
                if (i != 0 && gameTiles[i - 1][j].value == gameTiles[i][j].value)
                    return true;
                if (j != 0 && gameTiles[i][j - 1].value == gameTiles[i][j].value)
                    return true;
            }
        }
        return false;
    }
    public void randomMove(){
        int n = ((int) (Math.random() * 100)) % 4;
        switch (n){
            case 0: left();break;
            case 1: up();break;
            case 2: right();break;
            case 3: down();break;
        }
    }
    public boolean hasBoardChanged(){
        Tile[][] localTiles = previousStates.peek();
        for(int i = 0; i<gameTiles.length; i++){
            for(int j = 0; j<gameTiles[i].length; j++){
                if(localTiles[i][j].value!= gameTiles[i][j].value){
                    return true;
                }
            }
        }
        return false;
    }
    public MoveEfficiency getMoveEfficiency(Move move){
        move.move();
        if (!hasBoardChanged()) {
            rollback();
            return new MoveEfficiency(-1, 0, move);
        }

        int emptyTilesCount = 0;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].isEmpty()) {
                    emptyTilesCount++;
                }
            }
        }

        MoveEfficiency moveEfficiency = new MoveEfficiency(emptyTilesCount, score, move);
        rollback();

        return moveEfficiency;
    }
    public void autoMove(){
        PriorityQueue<MoveEfficiency> localQueue = new PriorityQueue<>(4,Collections.reverseOrder());
        localQueue.offer(getMoveEfficiency(this::up));
        localQueue.offer(getMoveEfficiency(this::down));
        localQueue.offer(getMoveEfficiency(this::right));
        localQueue.offer(getMoveEfficiency(this::left));
        localQueue.peek().getMove().move();
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }
}
