package com.caleb.smashshocker;

public class ScreenPosition {

    private int x;
    private int y;
    private int width;
    private int height;

    public ScreenPosition(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public enum DamagePositions {
        TWO_PLAYERS_ONE(new ScreenPosition(510, 920, 140, 72)),
        TWO_PLAYERS_TWO(new ScreenPosition(1250, 920, 143, 72)),
        THREE_PLAYERS_ONE(new ScreenPosition(260, 920, 140, 72)),
        THREE_PLAYERS_TWO(new ScreenPosition(882, 920, 140, 72)),
        THREE_PLAYERS_THREE(new ScreenPosition(1512, 920, 140, 72)),
        FOUR_PLAYERS_ONE(new ScreenPosition(290, 920, 140, 72)),
        FOUR_PLAYERS_TWO(new ScreenPosition(700, 920, 140, 72)),
        FOUR_PLAYERS_THREE(new ScreenPosition(1110, 920, 140, 72)),
        FOUR_PLAYERS_FOUR(new ScreenPosition(1520, 920, 140, 72)),
        ;

        private ScreenPosition damagePosition;
        DamagePositions(ScreenPosition damagePosition) {
            this.damagePosition = damagePosition;
        }

        public ScreenPosition getDamagePosition() {
            return damagePosition;
        }
    }

    public enum PlayerPositions {
        TWO_PLAYERS_ONE(new ScreenPosition(551, 1001, 157, 4),-443335), //16657952
        TWO_PLAYERS_TWO(new ScreenPosition(1286, 1001, 157, 4), 4751311), //4751311
        THREE_PLAYERS_ONE(new ScreenPosition(301, 1001, 157, 4), 16657952),
        THREE_PLAYERS_TWO(new ScreenPosition(923, 1001, 157, 4), 4751311),
        THREE_PLAYERS_THREE(new ScreenPosition(1543, 1001, 157, 4), 15647527), //15647527
        FOUR_PLAYERS_ONE(new ScreenPosition(331, 1001, 157, 4), 16657952),
        FOUR_PLAYERS_TWO(new ScreenPosition(741, 1001, 157, 4), 4751311),
        FOUR_PLAYERS_THREE(new ScreenPosition(1151, 1001, 157, 4), 15647527),
        FOUR_PLAYERS_FOUR(new ScreenPosition(1561, 1001, 157, 4), 4704337), // 4704337

        ;

        private ScreenPosition playerPosition;
        private int color;

        PlayerPositions(ScreenPosition playerPosition, int color) {
            this.playerPosition = playerPosition;
            this.color = color;
        }

        public ScreenPosition getPlayerPosition() {
            return playerPosition;
        }

        public int getColor() {
            return color;
        }

    }

    public enum PeriodPositions {
        FOUR_PLAYERS_ONE(new ScreenPosition(425 - 40, 941, 3, 3)),
        FOUR_PLAYERS_TWO(new ScreenPosition(830 - 40, 941, 3, 3)),
        FOUR_PLAYERS_THREE(new ScreenPosition(1240 - 40, 941, 3, 3)),
        FOUR_PLAYERS_FOUR(new ScreenPosition(1650 - 40, 941, 3, 3)),

        THREE_PLAYERS_ONE(new ScreenPosition(390 - 40, 941, 3, 3)),
        THREE_PLAYERS_TWO(new ScreenPosition(1015 - 40, 941, 3, 3)),
        THREE_PLAYERS_THREE(new ScreenPosition(1639 - 40, 941, 3, 3)),

//        TWO_PLAYERS_ONE(new DamagePosition(675, 973, 3, 3)),
        TWO_PLAYERS_ONE(new ScreenPosition(635, 930, 3, 3)),
//        TWO_PLAYERS_TWO(new DamagePosition(1415, 973, 3, 3)),
        TWO_PLAYERS_TWO(new ScreenPosition(1376, 930, 3, 3)),
        ;

        private ScreenPosition damagePosition;

        PeriodPositions(ScreenPosition damagePosition) {
            this.damagePosition = damagePosition;
        }

        public ScreenPosition getDamagePosition() {
            return damagePosition;
        }
    }




}
