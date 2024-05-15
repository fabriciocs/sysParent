import sys;
import subprocess
import os
import re
import cv2
import numpy as np
from PIL import Image, ImageDraw, ImageFont


def get_image_info(image_path):
    """ Use ImageMagick to get image dimensions """
    cmd = f'magick identify -format "%wx%h" "{image_path}"'
    process = subprocess.run(cmd, capture_output=True, text=True, shell=True)
    dimensions = process.stdout.strip()
    return dimensions

def create_adapted_logo(template_path, text, dimensions, output_path):
    """ Create an adapted logo by adding text to a template and resizing it """
    target_width, target_height = map(int, dimensions.split('x'))
    # Load the template logo
    logo = Image.open(template_path)
    # Calculate aspect ratio of the template logo
    original_width, original_height = logo.size
    aspect_ratio = original_width / original_height
    # Calculate the new dimensions to fit within the target width and height
    new_width = target_width
    new_height = int(new_width / aspect_ratio)
    if new_height > target_height:
        new_height = target_height
        new_width = int(new_height * aspect_ratio)
    # Resize the logo
    logo = logo.resize((new_width, new_height))
    # Create a blank canvas with the target dimensions
    adapted_logo = Image.new("RGB", (target_width, target_height), (255, 255, 255))
    # Paste the resized logo onto the canvas
    x_offset = (target_width - new_width) // 2
    y_offset = (target_height - new_height) // 2
    adapted_logo.paste(logo, (x_offset, y_offset))
    # Draw text over the adapted logo
    draw = ImageDraw.Draw(adapted_logo)
    font = ImageFont.load_default()  # Using default font, customize if needed
    text_width, text_height = draw.textbbox((0, 0), text, font=font)[2:]
    text_position = ((target_width - text_width) / 2, (target_height - text_height) / 2)
    draw.text(text_position, text, font=font, fill='white')  # Change text color if needed
    # Save the adapted logo
    adapted_logo.save(output_path)

def remove_background(image_path):
    # Load the image
    image = cv2.imread(image_path)
    # Convert to RGB
    image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
    # Prepare a mask based on color thresholding
    lower = np.array([240, 240, 240])  # Adjust these values based on your image's background color
    upper = np.array([255, 255, 255])
    mask = cv2.inRange(image_rgb, lower, upper)
    # Invert the mask to get the foreground
    foreground = cv2.bitwise_not(mask)
    # Apply the foreground mask to the image
    result = cv2.bitwise_and(image_rgb, image_rgb, mask=foreground)
    # Convert back to BGR for saving
    result_bgr = cv2.cvtColor(result, cv2.COLOR_RGB2BGR)
    # Save the result
    cv2.imwrite('no_bg.png', result_bgr)
    return 'no_bg.png'


def convert_to_bw_and_vectorize(input_image):
    # Convert image to black and white using ImageMagick for potrace
    bw_image_path = 'bw_image.pbm'
    subprocess.run(['magick', input_image, '-threshold', '50%', bw_image_path], check=True)

    # Use potrace to convert bitmap to SVG
    svg_output_path = './output.svg'
    subprocess.run(['./potrace/potrace', bw_image_path, '-s', '-o', svg_output_path], check=True)
    return svg_output_path
def replace_with_svg(directory, original_logo_path, text):
    """ Replace all images in the directory with SVG files """
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith(('.png', '.jpg', '.jpeg', '.svg')):
                full_path = os.path.join(root, file)
                dimensions = get_image_info(full_path)
                
                if file.endswith(('.png', '.jpg', '.jpeg')):
                   

                    adapted_logo_path = os.path.join(root, 'adapted_logo.jpg')
                    create_adapted_logo(original_logo_path, text, dimensions, adapted_logo_path)
                    print(f'Replacing: {full_path}')
                    os.replace(adapted_logo_path, full_path)
                    print(f'Replaced: {full_path}')
                else:  # If file is an SVG
                     # Remove background from original logo
                    adapted_logo_path = os.path.join(root, 'adapted_logo.jpg')
                    create_adapted_logo(original_logo_path, text, dimensions, adapted_logo_path)
                    
                    new_svg_path = os.path.splitext(full_path)[0] + '.png'
                    os.replace(adapted_logo_path, new_svg_path)
                    print(f'Replaced SVG: {full_path} with {new_svg_path}')


def replace_background_urls(directory):
    """Replace background URLs in CSS files"""
    for root, dirs, files in os.walk(directory):
        for file in files:
            if file.endswith(('.css', '.scss')):
                full_path = os.path.join(root, file)
                with open(full_path, 'r') as f:
                    css_content = f.read()

                updated_css_content = re.sub(
                    # Pattern to match background URL with .svg extension
                    r"background:\s*url\(\s*'([^']+)\.svg'\s*\)\s*no-repeat\s*center\s*top;",
                    # Replacement string with captured filename and .png extension
                    r"background: url('\1.png') no-repeat center top;",
                    css_content
                )

                # Write the updated CSS content back to the file
                with open(full_path, 'w') as f:
                    f.write(updated_css_content)

                print(f'Replaced background URLs in: {full_path}')
                
                
def create_favicon(original_logo_path, output_path):
    """ Create favicon.ico from logo image with resizing and cropping """
    # Load the original logo
    logo = Image.open(original_logo_path)
    # Calculate aspect ratio of the original logo
    aspect_ratio = logo.width / logo.height
    # Define o tamanho do favicon
    favicon_size = (256, 256)
    
    # Calcula as dimensões para o redimensionamento e o corte
    if aspect_ratio > 1:  # Se a largura for maior que a altura
        # Redimensiona o logo para que a altura seja igual ao tamanho do favicon
        resized_logo = logo.resize((int(favicon_size[1] * aspect_ratio), favicon_size[1]))
        # Calcula o ponto de corte para centralizar o logo
        left = (resized_logo.width - favicon_size[0]) / 2
        top = 0
        right = left + favicon_size[0]
        bottom = favicon_size[1]
    else:  # Se a altura for maior que a largura ou igual
        # Redimensiona o logo para que a largura seja igual ao tamanho do favicon
        resized_logo = logo.resize((favicon_size[0], int(favicon_size[0] / aspect_ratio)))
        # Calcula o ponto de corte para centralizar o logo
        left = 0
        top = (resized_logo.height - favicon_size[1]) / 2
        right = favicon_size[0]
        bottom = top + favicon_size[1]

    # Corta o logo para o tamanho do favicon
    cropped_logo = resized_logo.crop((left, top, right, bottom))

    # Cria um novo canvas para o favicon
    favicon = Image.new("RGBA", favicon_size, (255, 255, 255, 0))
    # Calcula o ponto de colagem para centralizar o logo
    x_offset = (favicon.width - cropped_logo.width) // 2
    y_offset = (favicon.height - cropped_logo.height) // 2
    # Cola o logo no canvas do favicon
    favicon.paste(cropped_logo, (x_offset, y_offset))

    # Salva o favicon.ico
    favicon.save(output_path, format="ICO")
    print(f'Favicon created: {output_path}')


def main():
    if len(sys.argv) != 2:
        print("Usage: script.py <logo_path>")
        sys.exit(1)

    original_logo_path = sys.argv[1]
    directory_to_fix = './src/main/webapp/content/images'
    text = "Custom Text"

    replace_with_svg(directory_to_fix, original_logo_path, text)
    replace_with_svg('./webpack', original_logo_path, text)
    directory_to_fix_bg = './src/main/webapp'
    create_favicon(original_logo_path, f'{directory_to_fix_bg}/favicon.ico')
    replace_background_urls(directory_to_fix_bg)

if __name__ == "__main__":
    main()
