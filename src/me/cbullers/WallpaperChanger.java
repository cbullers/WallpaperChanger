package me.cbullers;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.UINT_PTR;
import com.sun.jna.win32.*;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Created by cbullers on 10/23/2016.
 */
public class WallpaperChanger
{

    // main
    public static void main(String... args)
    {
        new AskWindow();
    }

    // SPI (http://stackoverflow.com/users/228171/mark-peters)
    public interface SPI extends StdCallLibrary {

        //from MSDN article
        long SPI_SETDESKWALLPAPER = 20;
        long SPIF_UPDATEINIFILE = 0x01;
        long SPIF_SENDWININICHANGE = 0x02;

        SPI INSTANCE = (SPI) Native.loadLibrary("user32", SPI.class, new HashMap<Object, Object>() {
            {
                put(OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
                put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
            }
        });

        boolean SystemParametersInfo(
                UINT_PTR uiAction,
                UINT_PTR uiParam,
                String pvParam,
                UINT_PTR fWinIni
        );
    }

}

// meh stuff
class AskWindow extends JFrame
{
    // fields
    JTextField urlInput = new JTextField();
    JButton ok = new JButton("Change It!");
    Image retrieved = null;

    // use
    String USER = System.getProperty("user.home");

    // static constructor
    public AskWindow() {
        this.setSize(500, 125);
        this.setLocation(500, 500);
        this.setTitle("Desktop Wallpaper Changer");
        this.setLayout(null);

        this.add(urlInput);
        this.add(ok);

        urlInput.setBounds(10,10,465,25);

        ok.setBounds(200,50,100,25);
        ok.addActionListener(okListener);

        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    // http://stackoverflow.com/users/1026805/sri-harsha-chilakapati
    public static BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    ActionListener okListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // All the stuff
            try
            {
                URL url = new URL(urlInput.getText());
                retrieved = ImageIO.read(url);
                String pathToWrite = USER+"\\Pictures\\";
                File create = new File(pathToWrite + "DesktopWallpaper.jpg");
                create.createNewFile();

                ImageIO.write(toBufferedImage(retrieved), "jpg", new File(pathToWrite + "DesktopWallpaper.jpg"));
                String path = pathToWrite + "DesktopWallpaper.jpg";
                WallpaperChanger.SPI.INSTANCE.SystemParametersInfo(
                        new UINT_PTR(WallpaperChanger.SPI.SPI_SETDESKWALLPAPER),
                        new UINT_PTR(0),
                        path,
                        new UINT_PTR(WallpaperChanger.SPI.SPIF_UPDATEINIFILE | WallpaperChanger.SPI.SPIF_SENDWININICHANGE)
                );

                System.exit(0);
            }
            catch(IOException er)
            {
                JOptionPane err = new JOptionPane("Bad URL!", JOptionPane.ERROR_MESSAGE);
                JDialog errr = err.createDialog("ERROR!");
                errr.setAlwaysOnTop(true);
                errr.setVisible(true);
            }
        }
    };

}