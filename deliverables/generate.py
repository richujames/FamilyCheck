from pathlib import Path
import json, zipfile, html, textwrap
from PIL import Image, ImageDraw, ImageFont
OUT = Path(__file__).parent.resolve()
NAVY, TEAL, BG, GRAY = '172E3C', '147D70', 'F4F7F8', '526773'
def esc(s): return html.escape(str(s), quote=True)
def font(n,b=False): return ImageFont.truetype('C:/Windows/Fonts/arialbd.ttf' if b else 'C:/Windows/Fonts/arial.ttf', n)
def wrap(text,f,width):
    out=[]
    for line in text.split('\n'):
        cur=''
        for word in line.split():
            nxt=(cur+' '+word).strip()
            if cur and f.getlength(nxt)>width: out.append(cur);cur=word
            else: cur=nxt
        out.append(cur)
    return out
data=json.loads((OUT/'content.json').read_text(encoding='utf-8'))
slides,pages=data['slides'],data['pages']
# Report PDF and page previews. Editable text is also supplied in DOCX.
images=[]
for i,(title,blocks) in enumerate(pages):
    im=Image.new('RGB',(1240,1754),'white');d=ImageDraw.Draw(im)
    d.rectangle((0,0,1240,22),fill='#'+TEAL)
    d.text((100,65),'FAMILYCHECK / MOBILE APP DEVELOPMENT',font=font(19,True),fill='#'+TEAL)
    y=130
    for line in wrap(title,font(40,True),1040):d.text((100,y),line,font=font(40,True),fill='#'+NAVY);y+=52
    y+=20
    for typ,txt in blocks:
        size=27 if typ=='h' else 25 if typ=='sub' else 23
        f=font(size,typ in ('h','sub'));step=35 if typ=='h' else 32
        if typ=='h':y+=10
        if typ=='b':txt='• '+txt
        for line in wrap(txt,f,1040):d.text((100,y),line,font=f,fill='#'+(TEAL if typ=='h' else NAVY));y+=step
        y+=12 if typ!='b' else 6
    assert y<1635,(i,y)
    d.line((100,1650,1140,1650),fill='#D6E1E4',width=2)
    d.text((100,1680),'Aravindh P  |  Richu James',font=font(18),fill='#'+GRAY)
    d.text((1080,1680),str(i+1).zfill(2),font=font(18),fill='#'+GRAY)
    im.save(OUT/f'report_preview_{i+1}.png');images.append(im)
import zlib
objects=[b'<< /Type /Catalog /Pages 2 0 R >>',b'']
kids=[]
for im in images:
    page_id=len(objects)+1;img_id=page_id+1;stream_id=page_id+2;kids.append(page_id)
    objects.append(f'<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595.2 841.92] /Resources << /XObject << /Im {img_id} 0 R >> >> /Contents {stream_id} 0 R >>'.encode())
    raw=zlib.compress(im.tobytes())
    objects.append(f'<< /Type /XObject /Subtype /Image /Width {im.width} /Height {im.height} /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /FlateDecode /Length {len(raw)} >>\nstream\n'.encode()+raw+b'\nendstream')
    stream=b'q 595.2 0 0 841.92 0 0 cm /Im Do Q'
    objects.append(f'<< /Length {len(stream)} >>\nstream\n'.encode()+stream+b'\nendstream')
objects[1]=f'<< /Type /Pages /Count {len(kids)} /Kids [{" ".join(str(k)+" 0 R" for k in kids)}] >>'.encode()
pdf=b'%PDF-1.4\n%\xe2\xe3\xcf\xd3\n';offsets=[0]
for n,obj in enumerate(objects,1):offsets.append(len(pdf));pdf+=f'{n} 0 obj\n'.encode()+obj+b'\nendobj\n'
start=len(pdf);pdf+=f'xref\n0 {len(objects)+1}\n0000000000 65535 f \n'.encode()
for off in offsets[1:]:pdf+=f'{off:010d} 00000 n \n'.encode()
pdf+=f'trailer\n<< /Size {len(objects)+1} /Root 1 0 R >>\nstartxref\n{start}\n%%EOF'.encode()
(OUT/'FamilyCheck_Report.pdf').write_bytes(pdf)
W='http://schemas.openxmlformats.org/wordprocessingml/2006/main'
R='http://schemas.openxmlformats.org/officeDocument/2006/relationships'
RELS='http://schemas.openxmlformats.org/package/2006/relationships'
CT='<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/>'
def wp(txt,size=22,bold=False,color=NAVY,after=140):
    runs='<w:br/>'.join(f'<w:t xml:space="preserve">{esc(t)}</w:t>' for t in txt.split('\n'))
    b='<w:b/>' if bold else ''
    return f'<w:p><w:pPr><w:spacing w:after="{after}" w:line="264" w:lineRule="auto"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii="Arial" w:hAnsi="Arial"/><w:sz w:val="{size}"/><w:color w:val="{color}"/>{b}</w:rPr>{runs}</w:r></w:p>'
body=''
for i,(title,blocks) in enumerate(pages):
    if i:body+='<w:p><w:r><w:br w:type="page"/></w:r></w:p>'
    body+=wp('FAMILYCHECK / MOBILE APP DEVELOPMENT',18,True,TEAL)+wp(title,34,True)
    for typ,txt in blocks:body+=wp('• '+txt if typ=='b' else txt,25 if typ=='h' else 22,typ in ('h','sub'),TEAL if typ=='h' else NAVY,90 if typ=='b' else 130)
body+='<w:sectPr><w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="900" w:right="1000" w:bottom="900" w:left="1000"/></w:sectPr>'
with zipfile.ZipFile(OUT/'FamilyCheck_Report.docx','w',zipfile.ZIP_DEFLATED) as z:
    z.writestr('[Content_Types].xml',CT+'<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/></Types>')
    z.writestr('_rels/.rels',f'<Relationships xmlns="{RELS}"><Relationship Id="rId1" Type="{R}/officeDocument" Target="word/document.xml"/></Relationships>')
    z.writestr('word/document.xml',f'<?xml version="1.0" encoding="UTF-8"?><w:document xmlns:w="{W}"><w:body>{body}</w:body></w:document>')
A='http://schemas.openxmlformats.org/drawingml/2006/main'
P='http://schemas.openxmlformats.org/presentationml/2006/main'
def emu(x):return int(x*914400)
def shape(id,x,y,w,h,fill,txt,size,color,bold):
    fx=f'<a:solidFill><a:srgbClr val="{fill}"/></a:solidFill>' if fill else '<a:noFill/>'
    paras=''.join(f'<a:p><a:pPr><a:lnSpc><a:spcPct val="110000"/></a:lnSpc><a:spcAft><a:spcPts val="600"/></a:spcAft></a:pPr><a:r><a:rPr lang="en-US" sz="{int(size*100)}" b="{int(bold)}"><a:solidFill><a:srgbClr val="{color}"/></a:solidFill><a:latin typeface="Arial"/></a:rPr><a:t>{esc(line)}</a:t></a:r><a:endParaRPr lang="en-US" sz="{int(size*100)}"/></a:p>' for line in txt.split('\n'))
    return f'<p:sp><p:nvSpPr><p:cNvPr id="{id}" name="Shape {id}"/><p:cNvSpPr txBox="1"/><p:nvPr/></p:nvSpPr><p:spPr><a:xfrm><a:off x="{emu(x)}" y="{emu(y)}"/><a:ext cx="{emu(w)}" cy="{emu(h)}"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom>{fx}<a:ln><a:noFill/></a:ln></p:spPr><p:txBody><a:bodyPr wrap="square" lIns="0" tIns="0" rIns="0" bIns="0"/><a:lstStyle/>{paras}</p:txBody></p:sp>'
group='<p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>'
with zipfile.ZipFile(OUT/'FamilyCheck_Presentation.pptx','w',zipfile.ZIP_DEFLATED) as z:
    overrides='<Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/><Override PartName="/ppt/slideMasters/slideMaster1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml"/><Override PartName="/ppt/slideLayouts/slideLayout1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml"/>'
    overrides+=''.join(f'<Override PartName="/ppt/slides/slide{i}.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>' for i in range(1,13))
    overrides+='<Override PartName="/ppt/theme/theme1.xml" ContentType="application/vnd.openxmlformats-officedocument.theme+xml"/>'
    z.writestr('[Content_Types].xml',CT+overrides+'</Types>')
    z.writestr('_rels/.rels',f'<Relationships xmlns="{RELS}"><Relationship Id="rId1" Type="{R}/officeDocument" Target="ppt/presentation.xml"/></Relationships>')
    ids=''.join(f'<p:sldId id="{255+i}" r:id="rId{i+1}"/>' for i in range(1,13))
    z.writestr('ppt/presentation.xml',f'<p:presentation xmlns:a="{A}" xmlns:r="{R}" xmlns:p="{P}"><p:sldMasterIdLst><p:sldMasterId id="2147483648" r:id="rId1"/></p:sldMasterIdLst><p:sldIdLst>{ids}</p:sldIdLst><p:sldSz cx="12192000" cy="6858000" type="screen16x9"/><p:notesSz cx="6858000" cy="9144000"/></p:presentation>')
    rr=f'<Relationship Id="rId1" Type="{R}/slideMaster" Target="slideMasters/slideMaster1.xml"/>'+''.join(f'<Relationship Id="rId{i+1}" Type="{R}/slide" Target="slides/slide{i}.xml"/>' for i in range(1,13))
    z.writestr('ppt/_rels/presentation.xml.rels',f'<Relationships xmlns="{RELS}">{rr}</Relationships>')
    z.writestr('ppt/slideMasters/slideMaster1.xml',f'<p:sldMaster xmlns:a="{A}" xmlns:r="{R}" xmlns:p="{P}"><p:cSld><p:spTree>{group}</p:spTree></p:cSld><p:clrMap accent1="accent1" accent2="accent2" accent3="accent3" accent4="accent4" accent5="accent5" accent6="accent6" bg1="lt1" bg2="lt2" folHlink="folHlink" hlink="hlink" tx1="dk1" tx2="dk2"/><p:sldLayoutIdLst><p:sldLayoutId id="2147483649" r:id="rId1"/></p:sldLayoutIdLst><p:txStyles><p:titleStyle/><p:bodyStyle/><p:otherStyle/></p:txStyles></p:sldMaster>')
    z.writestr('ppt/slideMasters/_rels/slideMaster1.xml.rels',f'<Relationships xmlns="{RELS}"><Relationship Id="rId1" Type="{R}/slideLayout" Target="../slideLayouts/slideLayout1.xml"/><Relationship Id="rId2" Type="{R}/theme" Target="../theme/theme1.xml"/></Relationships>')
    colors={'dk1':NAVY,'lt1':'FFFFFF','dk2':GRAY,'lt2':BG,'accent1':TEAL,'accent2':'6CA99D','accent3':'D5E9E4','accent4':'527A91','accent5':'C4A66A','accent6':'A0B4BD','hlink':TEAL,'folHlink':'527A91'}
    clr=''.join(f'<a:{k}><a:srgbClr val="{v}"/></a:{k}>' for k,v in colors.items())
    fill='<a:solidFill><a:schemeClr val="phClr"/></a:solidFill>'
    line='<a:ln w="9525" cap="flat" cmpd="sng" algn="ctr">'+fill+'<a:prstDash val="solid"/><a:miter lim="800000"/></a:ln>'
    z.writestr('ppt/theme/theme1.xml',f'<a:theme xmlns:a="{A}" name="FamilyCheck"><a:themeElements><a:clrScheme name="FamilyCheck">{clr}</a:clrScheme><a:fontScheme name="Arial"><a:majorFont><a:latin typeface="Arial"/><a:ea typeface=""/><a:cs typeface=""/></a:majorFont><a:minorFont><a:latin typeface="Arial"/><a:ea typeface=""/><a:cs typeface=""/></a:minorFont></a:fontScheme><a:fmtScheme name="Simple"><a:fillStyleLst>{fill*3}</a:fillStyleLst><a:lnStyleLst>{line*3}</a:lnStyleLst><a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle><a:effectStyle><a:effectLst/></a:effectStyle><a:effectStyle><a:effectLst/></a:effectStyle></a:effectStyleLst><a:bgFillStyleLst>{fill*3}</a:bgFillStyleLst></a:fmtScheme></a:themeElements><a:objectDefaults/><a:extraClrSchemeLst/></a:theme>')
    z.writestr('ppt/slideLayouts/slideLayout1.xml',f'<p:sldLayout xmlns:a="{A}" xmlns:r="{R}" xmlns:p="{P}" type="blank" preserve="1"><p:cSld name="Blank"><p:spTree>{group}</p:spTree></p:cSld><p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr></p:sldLayout>')
    z.writestr('ppt/slideLayouts/_rels/slideLayout1.xml.rels',f'<Relationships xmlns="{RELS}"><Relationship Id="rId1" Type="{R}/slideMaster" Target="../slideMasters/slideMaster1.xml"/></Relationships>')
    for i,(title,subtitle,left,right) in enumerate(slides,1):
        specs=[(0,0,13.333,.15,TEAL,'',1,NAVY,False),(.65,.43,12,.3,None,'MOBILE APP DEVELOPMENT / FAMILYCHECK',12,TEAL,True),(.65,1,12,.7,None,title,36,NAVY,True),(.65,1.78,12,.5,None,subtitle,19,GRAY,False)]
        for k,(head,txt) in enumerate([left,right]):
            x=.65+k*6.2
            specs.extend([(x,2.6,5.8,3.97,'FFFFFF','',1,NAVY,False),(x+.25,2.85,5.3,.5,None,head,17,TEAL,True),(x+.25,3.47,5.3,3.05,None,txt,19,NAVY,False)])
        specs.extend([(.65,6.97,10,.25,None,'Aravindh P | Richu James',11,GRAY,False),(12,6.97,.7,.25,None,f'{i:02d} / 12',11,GRAY,False)])
        shapes=''.join(shape(n+2,*sp) for n,sp in enumerate(specs))
        z.writestr(f'ppt/slides/slide{i}.xml',f'<p:sld xmlns:a="{A}" xmlns:r="{R}" xmlns:p="{P}"><p:cSld><p:bg><p:bgPr><a:solidFill><a:srgbClr val="{BG}"/></a:solidFill><a:effectLst/></p:bgPr></p:bg><p:spTree>{group}{shapes}</p:spTree></p:cSld><p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr></p:sld>')
        z.writestr(f'ppt/slides/_rels/slide{i}.xml.rels',f'<Relationships xmlns="{RELS}"><Relationship Id="rId1" Type="{R}/slideLayout" Target="../slideLayouts/slideLayout1.xml"/></Relationships>')
        im=Image.new('RGB',(1600,900),'#'+BG);d=ImageDraw.Draw(im)
        for x,y,w,h,fill,txt,size,col,b in specs:
            if fill:d.rectangle((x*120,y*120,(x+w)*120,(y+h)*120),fill='#'+fill)
            if txt:
                f=font(round(size*120/72),b);yy=y*120
                for line in wrap(txt,f,w*120):d.text((x*120,yy),line,font=f,fill='#'+col);yy+=size*120/72*1.1+10
                assert yy<=(y+h)*120+35,(i,txt,yy)
        im.save(OUT/f'slide_preview_{i}.png')
import xml.etree.ElementTree as ET, posixpath
for path in [OUT/'FamilyCheck_Presentation.pptx',OUT/'FamilyCheck_Report.docx']:
    with zipfile.ZipFile(path) as z:
        assert z.testzip() is None
        names=set(z.namelist())
        for name in names:
            if name.endswith(('.xml','.rels')):ET.fromstring(z.read(name))
            if name.endswith('.rels'):
                base=posixpath.dirname(posixpath.dirname(name)) if name!='_rels/.rels' else ''
                for rel in ET.fromstring(z.read(name)):
                    if rel.attrib.get('TargetMode')!='External':assert posixpath.normpath(posixpath.join(base,rel.attrib['Target'])) in names
print('Created and validated: 12-slide PPTX, 8-page PDF report, editable DOCX report.')
