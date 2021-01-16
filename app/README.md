# Generator Engine

## Apache POI
- `XWPFParagraph` is the entry point, which is scanned from the template
    - seems Apache POI cannot create Paragraph in header
- `XWPFRun` you can add within Paragraph using createRun()
    - Run is one "line" in Word
    - You could add text, formatting and picture in Run
- `CTR` is the meta-structure inside Run
    - you can set text via `XWPFRun`, but to remove text you need to use `removeT` within `CTR`
- `CTP` is the meta-structure inside Paragraph
    - you can move the `XmlCursor` from the paragraph, though it is not clear what it does for now.            