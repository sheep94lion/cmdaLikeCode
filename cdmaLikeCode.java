package com.zy.vlc;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.json.*;

/**
 * Created by yi on 16-10-14.
 */
public class cdmaLikeCode {
    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
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
        byte[] byteArray;
        byteArray = getByteArray(message);
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
        }
    }
    private static int[][] getTotalPattern(int[][] matrix, int totalX, int totalY, int patternWidth, byte[] byteArray, int n_bits, int[][][] patterns) {
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
    private static byte[] getByteArray(String message) {
        byte[] byteArray = message.getBytes();
        return byteArray;
    }
}
