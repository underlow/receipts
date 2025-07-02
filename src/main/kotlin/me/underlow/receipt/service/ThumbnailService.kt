package me.underlow.receipt.service

import org.springframework.stereotype.Service
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

/**
 * Service for generating thumbnails from various file types
 */
@Service
class ThumbnailService {

    /**
     * Generates a thumbnail for a file, supporting images and PDFs
     * Returns null if thumbnail generation fails or file type is unsupported
     */
    fun generateThumbnail(
        filePath: String, 
        filename: String, 
        width: Int, 
        height: Int
    ): ByteArray? {
        val file = File(filePath)
        if (!file.exists()) return null

        val extension = filename.substringAfterLast('.', "").lowercase()
        
        return try {
            when (extension) {
                "pdf" -> generatePdfPlaceholder(width, height)
                "jpg", "jpeg", "png", "gif", "bmp", "tiff", "tif" -> 
                    generateImageThumbnail(file, width, height)
                else -> null
            }
        } catch (e: Exception) {
            // Log error in production
            null
        }
    }

    /**
     * Generates a placeholder thumbnail for PDF files
     */
    private fun generatePdfPlaceholder(width: Int, height: Int): ByteArray? {
        return try {
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            val graphics = image.createGraphics()
            
            // Set background color
            graphics.color = java.awt.Color.LIGHT_GRAY
            graphics.fillRect(0, 0, width, height)
            
            // Draw PDF icon placeholder
            graphics.color = java.awt.Color.DARK_GRAY
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            
            // Draw a simple rectangle with "PDF" text
            val margin = width / 10
            graphics.fillRect(margin, margin, width - 2 * margin, height - 2 * margin)
            
            graphics.color = java.awt.Color.WHITE
            val font = graphics.font.deriveFont(width / 8f)
            graphics.font = font
            val fontMetrics = graphics.getFontMetrics(font)
            val textWidth = fontMetrics.stringWidth("PDF")
            val textHeight = fontMetrics.ascent
            graphics.drawString("PDF", 
                (width - textWidth) / 2, 
                (height + textHeight) / 2
            )
            
            graphics.dispose()
            imageToByteArray(image)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generates thumbnail from image file
     */
    private fun generateImageThumbnail(file: File, width: Int, height: Int): ByteArray? {
        return try {
            val originalImage = ImageIO.read(file) ?: return null
            val thumbnail = resizeImage(originalImage, width, height)
            imageToByteArray(thumbnail)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Resizes image maintaining aspect ratio
     */
    private fun resizeImage(originalImage: BufferedImage, targetWidth: Int, targetHeight: Int): BufferedImage {
        val originalWidth = originalImage.width
        val originalHeight = originalImage.height
        
        // Calculate scaling factor to maintain aspect ratio
        val scaleX = targetWidth.toDouble() / originalWidth
        val scaleY = targetHeight.toDouble() / originalHeight
        val scale = minOf(scaleX, scaleY)
        
        val scaledWidth = (originalWidth * scale).toInt()
        val scaledHeight = (originalHeight * scale).toInt()
        
        val resizedImage = BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB)
        val graphics: Graphics2D = resizedImage.createGraphics()
        
        // Enable anti-aliasing for better quality
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        
        graphics.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null)
        graphics.dispose()
        
        return resizedImage
    }

    /**
     * Converts BufferedImage to JPEG byte array
     */
    private fun imageToByteArray(image: BufferedImage): ByteArray {
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "jpeg", outputStream)
        return outputStream.toByteArray()
    }
}