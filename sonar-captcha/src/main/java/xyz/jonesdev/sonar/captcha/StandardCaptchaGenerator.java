/*
 * Copyright (C) 2024 Sonar Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.jonesdev.sonar.captcha;

import com.jhlabs.image.FBMFilter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.jonesdev.sonar.api.fallback.captcha.CaptchaGenerator;
import xyz.jonesdev.sonar.captcha.filters.CircleInverseFilter;
import xyz.jonesdev.sonar.captcha.filters.CurvesOverlayFilter;
import xyz.jonesdev.sonar.captcha.filters.NoiseOverlayFilter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;

import static java.awt.image.BufferedImage.TYPE_3BYTE_BGR;
import static xyz.jonesdev.sonar.captcha.StandardTTFFontProvider.FONTS;
import static xyz.jonesdev.sonar.captcha.StandardTTFFontProvider.STANDARD_FONT_SIZE;

@Getter
@RequiredArgsConstructor
public final class StandardCaptchaGenerator implements CaptchaGenerator {
  private static final CurvesOverlayFilter CURVES = new CurvesOverlayFilter(3);
  private static final CircleInverseFilter CIRCLES = new CircleInverseFilter(
    1, 30, 10);
  private static final NoiseOverlayFilter NOISE = new NoiseOverlayFilter(1, 20);
  private static final FBMFilter FBM = new FBMFilter();
  private static final Random RANDOM = new Random();
  private static final Color[] COLORS = new Color[4];
  private static final float[] COLOR_FRACTIONS = new float[COLORS.length];

  static {
    FBM.setAmount(0.6f);
    FBM.setScale(15);

    // Create fractions based on the number of colors
    for (int i = 0; i < COLOR_FRACTIONS.length; i++) {
      COLOR_FRACTIONS[i] = (float) i / (COLOR_FRACTIONS.length - 1);
    }
  }

  private final int width, height;
  private final @Nullable InputStream background;
  private BufferedImage backgroundImage;

  @Override
  public @NotNull BufferedImage createImage(final char[] answer) {
    final BufferedImage image = createBackgroundImage();
    final Graphics2D graphics = image.createGraphics();
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // Draw characters and other effects on the image
    applyRandomColorGradient(graphics);
    drawCharacters(graphics, answer);
    CURVES.transform(image, graphics);
    NOISE.transform(image);
    //CIRCLES.transform(image); // TODO: check if the text is still easy to read after this
    // Make sure to dispose the graphics instance
    graphics.dispose();
    return image;
  }

  private @NotNull BufferedImage createBackgroundImage() {
    if (background == null) {
      final BufferedImage image = new BufferedImage(width, height, TYPE_3BYTE_BGR);
      // Fill the entire image with a noise texture
      return FBM.filter(image, image);
    }
    try {
      return backgroundImage == null ? backgroundImage = ImageIO.read(background) : backgroundImage;
    } catch (IOException exception) {
      throw new IllegalStateException("Could not read background image", exception);
    }
  }

  private void applyRandomColorGradient(final @NotNull Graphics2D graphics) {
    // Randomize the colors for the gradient effect
    for (int i = 0; i < COLORS.length; i++) {
      final float random = 0.9f + RANDOM.nextFloat() * 0.1f;
      COLORS[i] = Color.getHSBColor(RANDOM.nextFloat(), random, random);
    }

    // Apply the random gradient effect
    graphics.setPaint(new LinearGradientPaint(0, 0, width, height,
      COLOR_FRACTIONS, COLORS, MultipleGradientPaint.CycleMethod.REFLECT));
  }

  private void drawCharacters(final @NotNull Graphics2D graphics,
                              final char @NotNull [] answer) {
    final FontRenderContext ctx = graphics.getFontRenderContext();
    final GlyphVector[] glyphs = new GlyphVector[answer.length];

    for (int i = 0; i < answer.length; i++) {
      // Create a glyph vector for the character with a random font
      final Font font = FONTS[RANDOM.nextInt(FONTS.length)];
      glyphs[i] = font.createGlyphVector(ctx, String.valueOf(answer[i]));
    }

    final double scalingXY = 5 - Math.min(answer.length, 5) * 0.65;

    // Calculate first X and Y positions
    final double totalWidth = Arrays.stream(glyphs)
      .mapToDouble(glyph -> glyph.getLogicalBounds().getWidth() * scalingXY - 1)
      .sum();
    double beginX = Math.max(Math.min(width / 2D - totalWidth / 2D, totalWidth), 0);
    double beginY = (height + STANDARD_FONT_SIZE / 2D) / 2D + scalingXY;

    // Draw each glyph one by one
    for (final GlyphVector glyph : glyphs) {
      final AffineTransform transformation = AffineTransform.getTranslateInstance(beginX, beginY);
      // Shear the glyph by a random amount
      final double shearXY = Math.sin(beginX + beginY) / 6;
      transformation.shear(shearXY, shearXY);
      // Scale the glyph to perfectly fit the image
      transformation.scale(scalingXY, scalingXY);
      // Draw the glyph to the buffered image
      final Shape transformedShape = transformation.createTransformedShape(glyph.getOutline());
      graphics.fill(transformedShape);
      // Make sure the next glyph isn't drawn at the same position
      beginX += glyph.getVisualBounds().getWidth() * scalingXY + 2;
      beginY += -10 + RANDOM.nextFloat() * 20;
    }
  }
}
