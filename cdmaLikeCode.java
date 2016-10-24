package com.zy.vlc;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import javax.imageio.ImageIO;

import com.backblaze.erasure.ReedSolomon;
import org.json.*;

/**
 * Created by yi on 16-10-14.
 */
public class cdmaLikeCode {
    private int length, height;
    private int totalX, totalY, patternWidth;
    private int[][] matrix;
    private int n_bits;
    private String message;
    private int dataShards, parityShards, shardSize;
    private int n_patterns;
    private int[][][] patterns;

    public void configure(String configureFilePath) throws IOException {
        String configString;
        JSONObject objConfig;
        try {
            configString = new String(Files.readAllBytes(Paths.get(configureFilePath)));
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        objConfig = new JSONObject(configString);
        length = objConfig.getInt("length");
        height = objConfig.getInt("height");
        patternWidth = objConfig.getInt("patternWidth");
        totalX = objConfig.getInt("x");
        totalY = objConfig.getInt("y");
        n_bits = objConfig.getInt("n_bits");
        message = objConfig.getString("message");
        dataShards = objConfig.getInt("dataShards");
        parityShards = objConfig.getInt("parityShards");
        shardSize = objConfig.getInt("shardSize");
        matrix = new int[totalY * patternWidth][totalX * patternWidth];
        n_patterns = (int)Math.pow(2, n_bits);
        patterns = new int[n_patterns][patternWidth][patternWidth];
        JSONArray patternsJSONArray = objConfig.getJSONArray("patterns");
        for (int i = 0; i < n_patterns; i++) {
            JSONArray patternJSONArray = patternsJSONArray.getJSONArray(i);
            for (int j = 0; j < patternWidth; j++) {
                JSONArray rowJSONArray = patternJSONArray.getJSONArray(j);
                for (int k = 0; k < patternWidth; k++) {
                    patterns[i][j][k] = rowJSONArray.getInt(k);
                }
            }
        }
    }

    public static void main(String[] args) {
        cdmaLikeCode myCdmaLikeCode = new cdmaLikeCode();
        try {
            myCdmaLikeCode.configure("/home/yi/IdeaProjects/cdma-like-code/src/com/zy/vlc/cdma_config.json");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        myCdmaLikeCode.generateCodeImage("/home/yi/Pictures/test.bmp");
        /*
        // get configuration
        String configString;
        try {
            configString = new String(Files.readAllBytes(Paths.get("/home/yi/IdeaProjects/cdma-like-code/src/com/zy/vlc/cdma_config.json")));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        JSONObject objConfig;
        objConfig = new JSONObject(configString);
        // get attributes of the configuration
        // get the length and the height of the output picture and init the picture
        int length, height;
        length = objConfig.getInt("length");
        height = objConfig.getInt("height");
        final BufferedImage res = new BufferedImage(length, height, BufferedImage.TYPE_INT_BGR);
        for (int x = 0; x < length; x++) {
            for (int y = 0; y < height; y++) {
                res.setRGB(x, y, Color.WHITE.getRGB());
            }
        }
        //matrix: 0 for the cell is white and 1 for the cell is black
        int totalX, totalY;
        int patternWidth;
        patternWidth = objConfig.getInt("patternWidth");
        totalX = objConfig.getInt("x");
        totalY = objConfig.getInt("y");
        int[][] matrix = new int[totalY * patternWidth][totalX * patternWidth];
        //get the patterns
        JSONArray patternsJSONArray = objConfig.getJSONArray("patterns");
        int n_bits = objConfig.getInt("n_bits");
        int n_patterns = (int)Math.pow(2, n_bits);
        int[][][] patterns = new int[n_patterns][patternWidth][patternWidth];
        for (int i = 0; i < n_patterns; i++) {
            JSONArray patternJSONArray = patternsJSONArray.getJSONArray(i);
            for (int j = 0; j < patternWidth; j++) {
                JSONArray rowJSONArray = patternJSONArray.getJSONArray(j);
                for (int k = 0; k < patternWidth; k++) {
                    patterns[i][j][k] = rowJSONArray.getInt(k);
                }
            }
        }
        //get byteArray
        String message = objConfig.getString("message");
        int dataShards, parityShards, shardSize;
        byte[] byteArray;
        dataShards = objConfig.getInt("dataShards");
        parityShards = objConfig.getInt("parityShards");
        shardSize = objConfig.getInt("shardSize");
        byteArray = getByteArray(message, dataShards, parityShards, shardSize);
        // get the bit map for the whole image
        matrix = getTotalPattern(matrix, totalX, totalY, patternWidth, byteArray, n_bits, patterns);
        for (int x = 0; x < length; x++) {
            for (int y = 0; y < height; y++) {
                int xCell = (int)((double)(x + 1) / length * (totalX * patternWidth));
                int yCell = (int)((double)(y + 1) / height * (totalY * patternWidth));
                if (xCell == totalX * patternWidth) {
                    xCell--;
                }
                if (yCell == totalY * patternWidth) {
                    yCell--;
                }
                //System.out.println(x);
                //System.out.println(y);
                //System.out.println(xCell);
                //System.out.println(yCell);
                if (matrix[yCell][xCell] == 1) {
                    res.setRGB(x, y, Color.BLACK.getRGB());
                } else {
                    res.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
        try {
            ImageIO.write(res, "bmp", new File("/home/yi/Pictures/test.bmp"));
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private void generateCodeImage(String outputFile) {
        final BufferedImage res = new BufferedImage(length, height, BufferedImage.TYPE_INT_BGR);
        for (int x = 0; x < length; x++) {
            for (int y = 0; y < height; y++) {
                res.setRGB(x, y, Color.WHITE.getRGB());
            }
        }
        byte[] byteArray;
        byteArray = getByteArray(message, dataShards, parityShards, shardSize);
        matrix = getTotalPattern(matrix, totalX, totalY, patternWidth, byteArray, n_bits, patterns);
        for (int x = 0; x < length; x++) {
            for (int y = 0; y < height; y++) {
                int xCell = (int)((double)(x + 1) / length * (totalX * patternWidth));
                int yCell = (int)((double)(y + 1) / height * (totalY * patternWidth));
                if (xCell == totalX * patternWidth) {
                    xCell--;
                }
                if (yCell == totalY * patternWidth) {
                    yCell--;
                }
                //System.out.println(x);
                //System.out.println(y);
                //System.out.println(xCell);
                //System.out.println(yCell);
                if (matrix[yCell][xCell] == 1) {
                    res.setRGB(x, y, Color.BLACK.getRGB());
                } else {
                    res.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }
        try {
            ImageIO.write(res, "bmp", new File(outputFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int[][] getTotalPattern(int[][] matrix, int totalX, int totalY, int patternWidth, byte[] byteArray, int n_bits, int[][][] patterns) {
        if (n_bits == 2) {
            int index = 0;
            for (int i = 0; i < totalY; i++) {
                for (int j = 0; j < totalX; j++) {
                    int i_byte = index / 4;
                    int remain = index % 4;
                    int target;
                    int shiftDis = 6 - remain * 2;
                    if (i_byte >= byteArray.length) {
                        target = 0;
                    } else {
                        target = byteArray[i_byte] >>> shiftDis & 3;
                    }
                    int[][] pattern = patterns[target];
                    int startY = i * patternWidth;
                    int startX = j * patternWidth;
                    for (int y = startY; y < startY + patternWidth; y++) {
                        for (int x = startX; x < startX + patternWidth; x++) {
                            matrix[y][x] = pattern[y - startY][x - startX];
                        }
                    }
                    index++;
                }
            }
        }
        return matrix;
    }

    private byte[] getByteArray(String message, int dataShards, int parityShards, int shardSize) {
        byte[][] shards = new byte[dataShards + parityShards][shardSize];
        byte[] byteArray = message.getBytes();
        for (int i = 0; i < dataShards; i++) {
            System.arraycopy(byteArray, i * shardSize, shards[i], 0, shardSize);
        }
        ReedSolomon reedSolomon = ReedSolomon.create(dataShards, parityShards);
        reedSolomon.encodeParity(shards, 0, shardSize);
        byte[] resultBytes = new byte[(dataShards + parityShards) * shardSize];
        for (int i = 0; i < (dataShards + parityShards); i++) {
            System.arraycopy(shards[i], 0, resultBytes, i * shardSize, shardSize);
        }
        return resultBytes;
    }
}
