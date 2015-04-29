target = "build/crawl"

# check directory
expected = ["rails-index.html", "maven-metadata.xml", "maven-metadata.xml.sha1", "3.0.9-index.html", "rails-3.0.9.gem.sha1", "rails-3.0.9.pom", "rails-3.0.9.pom.sha1"]

Dir["#{target}/*.*"].each do |f|
  unless expected.delete(File.basename(f))
	  raise "directory #{target} contains unexpected file #{File.basename(f)}"
  end
end
if expected.size > 0
  raise "did not download " + expected.join(',')
end

# check SHA1 files
Dir["#{target}/*sha1"].each do |f|
   if File.size(f) != 40
      raise "sha1 from #{f} has wrong length #{File.size(f)}"
   end
end

# check HTML
#TODO

# check XML
#TODO
