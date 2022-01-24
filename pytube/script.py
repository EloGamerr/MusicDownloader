import youtube_dl
from youtube_title_parse import get_artist_title
import sys
import os
from moviepy.editor import *

class MyLogger(object):
    def debug(self, msg):
        pass

    def warning(self, msg):
        pass

    def error(self, msg):
        pass

def download(url, raw_file):
    ydl_opts = {
        'format': 'bestaudio/best',
        'outtmpl': raw_file,
        'logger': MyLogger()
    }
    with youtube_dl.YoutubeDL(ydl_opts) as ydl:
        infos = ydl.extract_info(url)
        return infos["title"]

url = sys.argv[1]
output_path = sys.argv[2]
raw_file = output_path + 'raw_music.mp3'

title = download(url, raw_file)

""" Convert to MP3 """
new_file = output_path + title + '.mp3'
mp4_without_frames = AudioFileClip(raw_file)
mp4_without_frames.write_audiofile(new_file, verbose=False, logger=None)     
mp4_without_frames.close()
os.remove(raw_file)

artist = 'Unknown'

try:
    artist, title = get_artist_title(title)
except:
    pass

print(new_file)
print(title)
print(artist)