#!/usr/bin/env python3
"""
QR Code Generator for Yummy Restaurant Tables
Generates individual QR codes for each table (1-50) in JSON format
Requires: qrcode, pillow libraries
Install: pip install qrcode pillow
"""

import qrcode
import json
import os
from pathlib import Path

def generate_table_qr_codes(output_dir="qr_codes", restaurant_id=1, restaurant_name="Yummy Restaurant"):
    """
    Generate QR codes for all 50 restaurant tables
    
    Args:
        output_dir: Directory to save QR code images
        restaurant_id: Restaurant ID for QR code data
        restaurant_name: Restaurant name for QR code data
    """
    
    # Create output directory if it doesn't exist
    Path(output_dir).mkdir(parents=True, exist_ok=True)
    
    print(f"Generating QR codes for {restaurant_name}...")
    print(f"Output directory: {os.path.abspath(output_dir)}\n")
    
    success_count = 0
    error_count = 0
    
    # Generate QR codes for tables 1-50
    for table_id in range(1, 51):
        try:
            # Create QR data in JSON format
            qr_data = {
                "table_id": table_id,
                "restaurant_id": restaurant_id,
                "restaurant_name": restaurant_name
            }
            
            # Convert to JSON string
            json_str = json.dumps(qr_data, separators=(',', ':'))
            
            # Create QR code instance
            qr = qrcode.QRCode(
                version=1,           # Auto-adjust version based on data
                error_correction=qrcode.constants.ERROR_CORRECT_L,
                box_size=10,
                border=4,
            )
            
            # Add data and generate QR code
            qr.add_data(json_str)
            qr.make(fit=True)
            
            # Create image with custom colors
            img = qr.make_image(fill_color="black", back_color="white")
            
            # Save image
            filename = f"qr_table_{table_id:02d}.png"
            filepath = os.path.join(output_dir, filename)
            img.save(filepath)
            
            print(f"✓ Generated: {filename} (Table ID: {table_id}, Size: {img.size})")
            success_count += 1
            
        except Exception as e:
            print(f"✗ Error generating QR for table {table_id}: {e}")
            error_count += 1
    
    print(f"\n{'='*50}")
    print(f"Generation complete!")
    print(f"Successfully generated: {success_count} QR codes")
    if error_count > 0:
        print(f"Failed: {error_count} QR codes")
    print(f"Output location: {os.path.abspath(output_dir)}")
    print(f"{'='*50}")
    
    return success_count, error_count

def generate_single_qr(table_id, output_dir="qr_codes", restaurant_id=1, restaurant_name="Yummy Restaurant"):
    """
    Generate QR code for a single table
    
    Args:
        table_id: Table ID (1-50)
        output_dir: Directory to save QR code image
        restaurant_id: Restaurant ID for QR code data
        restaurant_name: Restaurant name for QR code data
    """
    
    if table_id < 1 or table_id > 50:
        print(f"Error: Table ID must be between 1 and 50")
        return False
    
    try:
        Path(output_dir).mkdir(parents=True, exist_ok=True)
        
        # Create QR data in JSON format
        qr_data = {
            "table_id": table_id,
            "restaurant_id": restaurant_id,
            "restaurant_name": restaurant_name
        }
        
        # Convert to JSON string
        json_str = json.dumps(qr_data, separators=(',', ':'))
        
        # Create QR code instance
        qr = qrcode.QRCode(
            version=1,
            error_correction=qrcode.constants.ERROR_CORRECT_L,
            box_size=10,
            border=4,
        )
        
        # Add data and generate QR code
        qr.add_data(json_str)
        qr.make(fit=True)
        
        # Create image
        img = qr.make_image(fill_color="black", back_color="white")
        
        # Save image
        filename = f"qr_table_{table_id:02d}.png"
        filepath = os.path.join(output_dir, filename)
        img.save(filepath)
        
        print(f"✓ Generated: {filename}")
        print(f"  Location: {os.path.abspath(filepath)}")
        print(f"  QR Content: {json_str}")
        print(f"  Size: {img.size}")
        
        return True
        
    except Exception as e:
        print(f"✗ Error generating QR for table {table_id}: {e}")
        return False

def generate_qr_pdf(output_file="restaurant_qr_codes.pdf"):
    """
    Generate all QR codes in a single PDF file (requires reportlab)
    
    Args:
        output_file: Output PDF filename
    """
    try:
        from reportlab.lib.pagesizes import letter, A4
        from reportlab.lib.units import inch
        from reportlab.pdfgen import canvas
        from io import BytesIO
        
        print("Generating PDF with all QR codes...")
        
        # Create PDF
        pdf_path = output_file
        c = canvas.Canvas(pdf_path, pagesize=A4)
        
        width, height = A4
        margin = 0.3 * inch
        qr_size = 1.8 * inch
        
        table_count = 0
        
        for table_id in range(1, 51):
            # Calculate position
            col = (table_id - 1) % 4
            row = (table_id - 1) // 4
            
            # New page if needed
            if table_count > 0 and table_count % 20 == 0:
                c.showPage()
            
            # Calculate x, y position
            x = margin + col * (qr_size + 0.2 * inch)
            y = height - margin - (row % 5 + 1) * (qr_size + 0.2 * inch)
            
            # Generate QR code
            qr_data = {
                "table_id": table_id,
                "restaurant_id": 1,
                "restaurant_name": "Yummy Restaurant"
            }
            json_str = json.dumps(qr_data, separators=(',', ':'))
            
            qr = qrcode.QRCode(version=1, box_size=8, border=2)
            qr.add_data(json_str)
            qr.make(fit=True)
            
            img = qr.make_image(fill_color="black", back_color="white")
            
            # Save QR to BytesIO
            img_buffer = BytesIO()
            img.save(img_buffer, format='PNG')
            img_buffer.seek(0)
            
            # Draw on PDF
            c.drawImage(img_buffer, x, y, width=qr_size, height=qr_size, preserveAspectRatio=True)
            c.drawString(x + qr_size/2 - 0.2*inch, y - 0.15*inch, f"Table {table_id:02d}")
            
            table_count += 1
        
        c.save()
        print(f"✓ PDF generated: {os.path.abspath(pdf_path)}")
        print(f"  Total tables: {table_count}")
        
    except ImportError:
        print("Error: reportlab not installed. Install with: pip install reportlab")

if __name__ == "__main__":
    import sys
    
    if len(sys.argv) > 1:
        if sys.argv[1] == "single":
            # Generate single QR code
            if len(sys.argv) > 2:
                table_id = int(sys.argv[2])
                generate_single_qr(table_id)
            else:
                print("Usage: python generate_qr.py single <table_id>")
        elif sys.argv[1] == "all":
            # Generate all QR codes
            generate_table_qr_codes()
        elif sys.argv[1] == "pdf":
            # Generate PDF
            generate_qr_pdf()
        else:
            print("Usage:")
            print("  Generate all QR codes: python generate_qr.py all")
            print("  Generate single QR code: python generate_qr.py single <table_id>")
            print("  Generate PDF with all QR codes: python generate_qr.py pdf")
    else:
        # Default: generate all
        generate_table_qr_codes()
