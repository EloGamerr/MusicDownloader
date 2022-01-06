from pytube import YouTube
from youtube_title_parse import get_artist_title
import sys
import os
from moviepy.editor import *

arg = sys.argv[1] 
output_path = sys.argv[2] 
yt = YouTube(arg)

mime_type = None
better_audio = None

for audio in yt.streams.filter(only_audio=True):
    if mime_type == None or audio.mime_type == mime_type or audio.mime_type == 'audio/mp4':
        mime_type = audio.mime_type
        better_audio = audio

file = better_audio.download(output_path)

""" Convert MP4 to MP3 """
base, ext = os.path.splitext(file)
new_file = base + '.mp3'
mp4_without_frames = AudioFileClip(file)     
mp4_without_frames.write_audiofile(new_file, verbose=False, logger=None)     
mp4_without_frames.close()
os.remove(file)

artist, title = get_artist_title(better_audio.title)

print(new_file)
print(title)
print(artist)
